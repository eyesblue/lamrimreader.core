package eyes.blue.lamrimreader.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by father on 16/5/17.
 */
public class SubtitleUtil {
	/*
	 * 回傳該數字所代表的音檔識別字串(001A, 001B, 002A, 002B, 003A ... 160B)，輸入範圍介於 0 ~ 319
	 * */
	public static String getNameId(int i){
		String result=null;
		try{
			int num=(i/2)+1;
			char sign=((i%2==0)?'A':'B');
			result=String.format("%03d", num)+sign;
		}catch(ArrayIndexOutOfBoundsException e){
			throw new ArrayIndexOutOfBoundsException();
		}
				
		return result;
	}

	/*
	 * 將音檔識別字串轉換成數字代號(001A=0, 001B=1, 002A=2, 002B=3, 003A=4 ... 160B=319)
	 * */
	public static int getNameToId(String str){
		int res=-1;
		str=str.toUpperCase();
		if(str.matches("\\d+A") || str.matches("\\d+B")){
			res=Integer.parseInt(str.substring(0, str.length()-1));
			res*=2;
			if(str.charAt(str.length()-1)=='A')
				res-=2;
			else if(str.charAt(str.length()-1)=='B')
				res-=1;
			else return -1;
		}
		
		return res;
	}
	
    /*
		 * While start playing, there may not have subtitle yet, it will return -1, except array index n.
		 * */
    public static int subtitleBSearch(Subtitle[] a, int key){
        int mid = a.length / 2;
        int low = 0;
        int hi = a.length;
        while (low <= hi) {
            mid = (low + hi) >>> 1;
             int d = 0;
            if (mid == 0) {
                if( key < a[0].startTimeMs ) return 0;
                if( a[1].startTimeMs <=  key) return 1;
                return 0;
            }
            if (mid == a.length - 1) {
                if(key<a[a.length-1].startTimeMs)return a.length - 2;
                return a.length - 1;
            }
            if (a[mid].startTimeMs > key && key <= a[mid + 1].startTimeMs) 
                d = 1;
            else if (a[mid].startTimeMs <= key && key < a[mid + 1].startTimeMs) 
                d = 0;
             else 
                d = -1;
            
            if (d == 0)
                return mid;
            else if (d > 0)
                hi = mid - 1;
            else
                low = ++mid;
        }
        String msg = "Binary search state error, shouldn't go to the unknow stage. this may cause by a not sorted subtitle: MID="
                + mid+ ", Compare "+ a[mid].startTimeMs	+ " <> "+ key+ " <> " + a[mid + 1].startTimeMs + " into unknow state.";
        new Exception(msg).printStackTrace();
        return -1;
    }

    public static Subtitle[] loadSubtitle(InputStream is) {
        ArrayList<Subtitle> subtitleList = new ArrayList<Subtitle>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String stemp;
            int lineCounter = 0;
            int step = 0; 
            // 0: Find the serial number, 
            // 1: Get the serial number, 
            // 2: Get the time description, 
            // 3: Get Subtitle
            int serial = 0;
            Subtitle se = null;

            while ((stemp = br.readLine()) != null) {
                lineCounter++;

                // This may find the serial number
                if (step == 0) {
                    if (stemp.matches("[0-9]+")) {
                        se = new Subtitle();
                        serial = Integer.parseInt(stemp);
                        step = 1;
                    }
                }

                // This may find the time description
                else if (step == 1) {
                    if (stemp.matches("[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3} +-+> +[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}")) {
                        String[] region=stemp.split(" +-+> +");
                        region[0]=region[0].trim();
                        region[1]=region[1].trim();
                        int timeMs;

                        String ts = region[0].substring(0, 2);
                        timeMs = Integer.parseInt(ts) * 3600000;
                        ts = region[0].substring(3, 5);
                        timeMs += Integer.parseInt(ts) * 60000;
                        ts = region[0].substring(6, 8);
                        timeMs += Integer.parseInt(ts) * 1000;
                        ts = region[0].substring(9, 12);
                        timeMs += Integer.parseInt(ts);
                        se.startTimeMs = timeMs;

                        ts = region[1].substring(0, 2);
                        timeMs = Integer.parseInt(ts) * 3600000;
                        ts = region[1].substring(3, 5);
                        timeMs += Integer.parseInt(ts) * 60000;
                        ts = region[1].substring(6, 8);
                        timeMs += Integer.parseInt(ts) * 1000;
                        ts = region[1].substring(9, 12);
                        timeMs += Integer.parseInt(ts);
                        se.endTimeMs = timeMs;
                        step = 2;
                    } 
                    else {
                        step = 0;
                    }
                } else if (step == 2) {
                    se.text = stemp;
                    step = 0;
                    subtitleList.add(se);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subtitleList.toArray(new Subtitle[0]);
    }

    public static String getMsToHMS(int ms){
        return getMsToHMS(ms,"'","\"",true);
    }

    public static String getMsToHMS(int ms,String minuteSign,String secSign,boolean hasDecimal){
        String sub=""+(ms%1000);
        if(sub.length()==1)sub="00"+sub;
        else if(sub.length()==2)sub="0"+sub;

        int second=ms/1000;
        int ht=second/3600;
        second=second%3600;
        int mt=second/60;
        second=second%60;

        String hs=""+ht;
        if(hs.length()==1)hs="0"+hs;
        String mst=""+mt;
        if(mst.length()==1)mst="0"+mst;
        String ss=""+second;
        if(ss.length()==1)ss="0"+ss;

        return mst+minuteSign+ss+((hasDecimal)?"."+sub:"")+secSign;
    }
}
