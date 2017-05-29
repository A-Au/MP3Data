import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

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
