import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

public class MP3Constants {

    // TODO: Convert to an enum to include "FREE" and "BAD" values?
    // USAGE: BITRATE[Version Index][Bit Rate Index]
    public final static int[][] BITRATE = {
            {8,     16,     24,     32,     40,     48,     56,     64,     80,     96,     112,    128,    144,    160},
            {32,    48,     56,     64,     80,     96,     112,    128,    144,    160,    176,    192,    224,    256},
            {32,    40,     48,     56,     64,     80,     96,     112,    128,    160,    192,    224,    256,    320},
            {32,    48,     56,     64,     80,     96,     112,    128,    160,    192,    224,    256,    320,    384},
            {32,    64,     96,     128,    160,    192,    224,    256,    288,    320,    352,    384,    416,    448}};

    public enum ChannelMode {
        STEREO          (0),    // 00
        JOINT_STEREO    (1),    // 01
        DUAL_CHANNEL    (2),    // 10
        MONO            (3);    // 11

        private int modeId;

        private ChannelMode(int modeId){
            this.modeId = modeId;
        }

        private final static Map<Integer, ChannelMode> map =
                stream(ChannelMode.values()).collect(toMap(mode -> mode.modeId, mode -> mode));

        public static ChannelMode valueOf(int modeId){
            return map.get(modeId);
        }
    }

    public enum LayerVersion {
        RESERVED,   // 00
        LAYER_3,    // 01
        LAYER_2,    // 10
        LAYER_1;    // 11
    }

    public enum MPEGVersion {
        MPEG_VER_2_5,   // 00
        RESERVED,       // 01
        MPEG_VER_2,     // 10
        MPEG_VER_1;     // 11
    }

}
