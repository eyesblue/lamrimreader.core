package eyes.blue.lamrimreader.core;

/**
 * Created by father on 16/5/19.
 */
public class Dot {
    public int line=-1, index=-1;
    public float sizeRate=1f;
    public char c;

    public Dot(int line, int index, float sizeRate, char c){
        this.line=line;
        this.index=index;
        this.sizeRate=sizeRate;
        this.c=c;
    }
}
