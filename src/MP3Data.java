import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MP3Data {

    private File mp3File;
    private int frameSize = -1;
    private int firstFrame = -1;

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

    private int[] readIS(InputStream is, int len) throws IOException {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = is.read();
        }
        return arr;
    }

    private void processMp3(InputStream is) throws IOException {
        int len = 64000;
        int[] arr = readIS(is, len);
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
