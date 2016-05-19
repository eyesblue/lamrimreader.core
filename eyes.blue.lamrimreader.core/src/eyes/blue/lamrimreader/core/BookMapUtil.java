package eyes.blue.lamrimreader.core;


/**
 * Created by father on 16/5/19.
 */
public class BookMapUtil {
    public final static int MEDIA=0;
    public final static int SUBTITLE=1;
    public final static int PAGE=2;
    public final static int LINE=3;
    public final static int WORD=4;
    public final static int LENGTH=5;

    public static int[][] getMaps(String data){
        if(data.length()==0) return null;

        String[] recStr=data.split(";");
        int[][] rec=new int[recStr.length][6];
        PLIndex[] plIndexs=new PLIndex[recStr.length];

        for(int i=0;i<recStr.length;i++){
            String[] eleStr=recStr[i].split(",");
            for(int j=0;j<eleStr.length;j++){
                try{
                    rec[i][j]=Integer.parseInt(eleStr[j]);
                }catch(ArrayIndexOutOfBoundsException aioob){
                    System.out.println("BookMapUtil: "+ recStr[i]+" error.");
                }
            }
        }

        return rec;
    }
}
