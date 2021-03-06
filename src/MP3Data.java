import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MP3Data {

    private File mp3File;
    private int frameSize;
    private int firstFrame;
    private int mp3HeaderInt;
    private int[] mp3Header;
    private MP3Constants.MPEGVersion mpegVersion;
    private MP3Constants.LayerVersion layerVersion;
    private int sampleRateFreq;
    private MP3Constants.ChannelMode channelMode;

    public MP3Data(String pathToFile) throws Exception {
        this.mp3File = new File(pathToFile);
        processMp3(new FileInputStream(this.mp3File));
    }

    public MP3Data(File mp3File) throws Exception {
        this.mp3File = mp3File;
        processMp3(new FileInputStream(this.mp3File));
    }
    
    public int getMP3FrameSize() { return frameSize; }

    public int getMP3FirstFrame() { return firstFrame; }

    public int getMp3Header() { return mp3HeaderInt; }

    public int[] getMp3HeaderArray() { return mp3Header; }

    public MP3Constants.MPEGVersion getMPEGAudioVersion() { return mpegVersion; }

    public MP3Constants.LayerVersion getLayer() { return layerVersion; }

    public boolean isProtected() { return (mp3Header[1] & 1) == 1; }

    // Returns bit rate in kbps, if -1, bad or free
    public int getBitRate() {
        int vInd = getVInd();
        int bitInd = ((mp3Header[2] >> 4) & 15) - 1;
        return (bitInd >= 0 && bitInd < 15)? MP3Constants.BITRATE[vInd][bitInd] : -1;
    }

    // Returns sample rate frequency, if -1 then it is a reserved value
    public int getSampleRateFrequency(){ return sampleRateFreq; }

    // if true, then padded with one extra slot
    public boolean isPadded(){ return ((mp3Header[2] >> 1) & 1) == 1; }

    public MP3Constants.ChannelMode getChannelMode(){ return channelMode; }

    // TODO: Mode extension

    public boolean isCopyright(){ return ((mp3Header[3] >> 3) & 1) == 1; }

    public boolean isOriginalMedia(){ return ((mp3Header[3] >> 2) & 1) == 1; }

    // TODO: Emphasis

    /*************************************************************
     *                                                           *
     * Helper Functions                                          *
     *                                                           *
     *************************************************************/

    private void processMp3(InputStream is) throws IOException {
        final int LEN = 64000;

        int[] arr = readIS(is, LEN);
        findHeader(arr, LEN);
        mp3Header = getHeader(arr);
        mp3HeaderInt = headerToInt(arr);
        mpegVersion = determineMPEGVersion();
        layerVersion = determineLayerVersion();
        sampleRateFreq = determineSampleRateFrequency();
        channelMode = determineChannelMode();
    }

    private int[] readIS(InputStream is, int len) throws IOException {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = is.read();
        }
        return arr;
    }

    private int headerToInt(int[] arr) {
        return arr[0] << 24 | (arr[1] & 0xFF) << 16 | (arr[2] & 0xFF) << 8 | (arr[3] & 0xFF);
    }

    private int[] getHeader(int[] arr) {
        int[] hdr = new int[4];
        for (int i = 0; i < 4; i++) {
            hdr[i] = arr[firstFrame + i];
        }
        return hdr;
    }

    private void findHeader(int[] arr, int len) {
        int pos = -1;
        for (int i = 0; i < len; i += 2) {
            if (arr[i] == 0xff && arr[i + 1] >= 0xe0 && arr[i + 1] <= 0xff) {
                if (pos != -1 && frameSize == -1) {
                    frameSize = i - pos;
                    if (arr[i + frameSize] == 0xff && arr[i + frameSize + 1] >= 0xe0
                            && arr[i + frameSize + 1] <= 0xff) {
                        if (firstFrame == -1) {
                            firstFrame = pos;
                        }
                        break;
                    } else {
                        frameSize = -1;
                    }
                }
                if (pos == -1) {
                    pos = i;
                }
            }
            if (i == len - 1 && frameSize == -1) {
                i = pos + 1;
                pos = -1;
            }
        }
    }

    private MP3Constants.LayerVersion determineLayerVersion(){
        int ver = (mp3Header[1] >> 1) & 3;
        // TODO: add map to this enum
        MP3Constants.LayerVersion layerVer = null;
        switch (ver) {
            case 0:
                layerVer = MP3Constants.LayerVersion.RESERVED;
                break;
            case 1:
                layerVer = MP3Constants.LayerVersion.LAYER_3;
                break;
            case 2:
                layerVer = MP3Constants.LayerVersion.LAYER_2;
                break;
            case 3:
                layerVer = MP3Constants.LayerVersion.LAYER_1;
                break;
        }
        return layerVer;
    }

    private MP3Constants.MPEGVersion determineMPEGVersion(){
        int ver = (mp3Header[1] >> 3) & 3;
        // TODO: add map to this enum
        MP3Constants.MPEGVersion mpegVer = null;
        switch(ver){
            case 0:
                mpegVer = MP3Constants.MPEGVersion.MPEG_VER_2_5;
                break;
            case 1:
                mpegVer = MP3Constants.MPEGVersion.RESERVED;
                break;
            case 2:
                mpegVer = MP3Constants.MPEGVersion.MPEG_VER_2;
                break;
            case 3:
                mpegVer = MP3Constants.MPEGVersion.MPEG_VER_1;
                break;
        }
        return mpegVer;
    }

    private int determineSampleRateFrequency(){
        int freqInd = (mp3Header[2] >> 2) & 3;
        int freq = -1;
        if (freqInd == 0){
            freq = 44100;
        } else if (freqInd == 1){
            freq = 48000;
        } else if (freqInd == 2){
            freq = 32000;
        }
        if (freq != -1){
            if (mpegVersion == MP3Constants.MPEGVersion.MPEG_VER_2){
                freq /= 2;
            } else if (mpegVersion == MP3Constants.MPEGVersion.MPEG_VER_2_5){
                freq /= 4;
            }
        }
        return freq;
    }

    private MP3Constants.ChannelMode determineChannelMode(){
        int id = (mp3Header[3] >> 6) & 3;
        return MP3Constants.ChannelMode.valueOf(id);
    }

    // Returns the first index according to the version values for the 2-dimensional array of bit rates,
    // MP3Constants.BITRATE[vInd][bitInd]
    private int getVInd(){
        int ind = (mp3Header[1] >> 1) & 15;
        if (ind == 9 || ind == 10){
            ind = 0;
        } else if (ind == 11){
            ind = 1;
        } else if (ind >= 13 && ind <= 15){
            ind -= 11;
        }
        return ind;
    }
}
