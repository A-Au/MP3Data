import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MP3Data {

    private File mp3File;
    private int frameSize = -1;
    private int firstFrame = -1;
    private int mp3HeaderInt;
    private int[] mp3Header;

    public MP3Data(String pathToFile) throws Exception {
        this.mp3File = new File(pathToFile);
        processMp3(new FileInputStream(this.mp3File));
    }

    public MP3Data(File mp3File) throws Exception {
        this.mp3File = mp3File;
        processMp3(new FileInputStream(this.mp3File));
    }
    
    public int getMP3FrameSize() {
        return frameSize;
    }

    public int getMP3FirstFrame() {
        return firstFrame;
    }

    public int getMp3Header() {
        return mp3HeaderInt;
    }

    public int[] getMp3HeaderArray() {
        return mp3Header;
    }

    public MPEGVersion getMPEGAudioVersion() {
        int ver = (mp3Header[1] >> 3) & 3;
        MPEGVersion mpegVer = null;
        switch(ver){
        case 0:
            mpegVer = MPEGVersion.MPEG_VER_2_5;
            break;
        case 1:
            mpegVer = MPEGVersion.RESERVED;
            break;
        case 2:
            mpegVer = MPEGVersion.MPEG_VER_2;
            break;
        case 3:
            mpegVer = MPEGVersion.MPEG_VER_1;
            break;
        }
        return mpegVer;
    }

    public LayerVersion getLayer() {
        int ver = (mp3Header[1] >> 1) & 3;
        LayerVersion layerVer = null;
        switch (ver) {
        case 0:
            layerVer = LayerVersion.RESERVED;
            break;
        case 1:
            layerVer = LayerVersion.LAYER_3;
            break;
        case 2:
            layerVer = LayerVersion.LAYER_2;
            break;
        case 3:
            layerVer = LayerVersion.LAYER_1;
            break;
        }
        return layerVer;
    }

    // Returns bit rate in kbps, if -1, bad or free
    // TODO: Is there a better way to do this?
    public int getBitRate() {
        int bitRate = -1;
        int ver = (mp3Header[1] >> 1) & 15;
        int ind = (mp3Header[2] >> 4) & 15;
        if (ind == 0 || ind == 15) {
            return -1;
        }
        ind = (ver & 0xF) << 4 | (ind & 0xF);
        switch (ver) {
        case 161:
        case 145:
        case 33:
        case 17:
            bitRate = 8;
            break;
        case 162:
        case 146:
        case 34:
        case 18:
            bitRate = 16;
            break;
        case 163:
        case 147:
        case 35:
        case 19:
            bitRate = 24;
            break;
        case 241:
        case 225:
        case 209:
        case 49:
        case 177:
        case 164:
        case 148:
        case 36:
        case 20:
            bitRate = 32;
            break;
        case 210:
        case 165:
        case 149:
        case 37:
        case 21:
            bitRate = 40;
            break;
        case 226:
        case 211:
        case 178:
        case 50:
        case 166:
        case 150:
        case 38:
        case 22:
            bitRate = 48;
            break;
        case 227:
        case 212:
        case 179:
        case 51:
        case 167:
        case 151:
        case 39:
        case 23:
            bitRate = 56;
            break;
        case 242:
        case 228:
        case 213:
        case 180:
        case 52:
        case 168:
        case 152:
        case 40:
        case 24:
            bitRate = 64;
            break;
        case 229:
        case 214:
        case 181:
        case 53:
        case 169:
        case 153:
        case 41:
        case 25:
            bitRate = 80;
            break;
        case 243:
        case 230:
        case 215:
        case 182:
        case 54:
        case 170:
        case 154:
        case 42:
        case 26:
            bitRate = 96;
            break;
        case 231:
        case 216:
        case 183:
        case 55:
        case 171:
        case 155:
        case 43:
        case 27:
            bitRate = 112;
            break;
        case 244:
        case 232:
        case 217:
        case 184:
        case 56:
        case 172:
        case 156:
        case 44:
        case 28:
            bitRate = 128;
            break;
        case 185:
        case 57:
        case 173:
        case 157:
        case 45:
        case 29:
            bitRate = 144;
            break;
        case 245:
        case 233:
        case 218:
        case 186:
        case 58:
        case 174:
        case 158:
        case 46:
        case 30:
            bitRate = 160;
            break;
        case 246:
        case 234:
        case 219:
        case 188:
        case 60:
            bitRate = 192;
            break;
        case 187:
        case 59:
            bitRate = 176;
            break;
        case 247:
        case 235:
        case 220:
        case 189:
        case 61:
            bitRate = 224;
            break;
        case 248:
        case 236:
        case 221:
        case 190:
        case 62:
            bitRate = 256;
            break;
        case 249:
            bitRate = 288;
            break;
        case 250:
        case 237:
        case 222:
            bitRate = 320;
            break;
        case 251:
            bitRate = 352;
            break;
        case 252:
        case 238:
            bitRate = 384;
            break;
        case 253:
            bitRate = 416;
            break;
        case 254:
            bitRate = 448;
            break;
        }
        return bitRate;
    }

    public boolean isProtected() {
        return (mp3Header[1] & 1) == 1;
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

    private void processMp3(InputStream is) throws IOException {
        final int LEN = 64000;

        int[] arr = readIS(is, LEN);
        findHeader(arr, LEN);
        mp3Header = getHeader(arr);
        mp3HeaderInt = headerToInt(arr);
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
}
