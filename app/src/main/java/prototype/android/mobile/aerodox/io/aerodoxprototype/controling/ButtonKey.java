package prototype.android.mobile.aerodox.io.aerodoxprototype.controling;

/**
 * Created by xia on 2/22/15.
 */
public enum ButtonKey {
    LEFT(1),
    MIDDLE(2),
    RIGHT(3);

    private final int value;

    private ButtonKey(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}
