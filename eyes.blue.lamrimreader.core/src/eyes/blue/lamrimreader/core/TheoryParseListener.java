package eyes.blue.lamrimreader.core;

import java.util.ArrayList;

/**
 * Created by father on 16/5/15.
 */
public class TheoryParseListener {
    public void onSegmentFound(int line, int index, int length, String segText, boolean isBold, boolean isNum, boolean isSmall){}
    public void onNewLineFound(int line, int index){}
    public void onFinishParse(ArrayList<Dot> dotList){};
}
