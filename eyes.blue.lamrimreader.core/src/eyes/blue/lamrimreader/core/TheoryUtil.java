package eyes.blue.lamrimreader.core;
import java.util.ArrayList;

/**
 * Created by father on 16/5/15.
 */
public class TheoryUtil {
    boolean debug=true;
    float smallRate=0.9f;
    public final static int TO_START=1;
    public final static int TO_END=2;
    String[] bookContent=null;

    public TheoryUtil(String[] bookStrArray){
        this.bookContent=bookStrArray;
    }

    /*
    * 解讀 pageNum 參數頁的內容，解讀過程中會依據所解讀到的內容送給 listener 內對應的函示中。
    * */
    public void parsePage(int pageNum, TheoryParseListener listener){
        parseText(bookContent[pageNum], listener);
    }

    /*
    * 解讀傳入的字串，解讀過程中會依據所解讀到的內容送給 listener 內對應的函示中。
    * */
    public void parseText(String text, TheoryParseListener listener){
        int lineCounter=0;
        int start=0,end=0;
        ArrayList<Dot> dotList=new ArrayList<>();
        boolean onCmd=false;
        boolean isBold=false, isNum=false, isSmall=false;
        StringBuffer line=new StringBuffer();

        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(onCmd){
                if(c!='>'){end++;continue;}
                if(debug) System.out.println("Find a command stop");
                onCmd=false;

                switch(text.charAt(start)){
                    case '/':
                        switch(text.charAt(start+1)){
                            case 'b':if(debug)System.out.println("release bold command");isBold=false;break;
                            case 'n':if(debug)System.out.println("release num command");isNum=false;;break;
                            case 's':if(debug)System.out.println("release small command");isSmall=false;break;
                        };
                        break;
                    case 'b':if(debug)System.out.println("set bold command");isBold=true;break;
                    case 'n':if(debug)System.out.println("set num command");isNum=true;break;
                    case 's':if(debug)System.out.println("set small command");isSmall=true;break;
                }
                start=i+1;
                end=start;
            }
            else if(c=='‧' || c=='。'){
                    String str=text.substring(start, end);
                    listener.onSegmentFound(lineCounter, start, end-start, str, isBold, isNum, isSmall);
                    line.append(str);
                    dotList.add(new Dot(lineCounter, line.length(), ((isSmall)?smallRate:1), c));
                if(debug)System.out.println("Print "+text.substring(start, end)+", start: "+start+", end: "+end+", ("+(end-start)+")");
                start=i+1;
                end=start;
                continue;
            }
            else if(c=='\n'){
                String str=text.substring(start, end);
                if(!(str.length()==1 && (str.charAt(0)=='‧' || str.charAt(0)=='。'))) {
                    listener.onSegmentFound(lineCounter, start, end - start, str, isBold, isNum, isSmall);
                    listener.onNewLineFound(lineCounter, start);
                }
                line.delete(0,line.length());
                start=i+1;
                end=start;
                lineCounter++;
                continue;
            }
            else if(c=='<'){
                if(debug)System.out.println("Find a command start");
                if(end-start>0){
                    String str=text.substring(start, end);
                    listener.onSegmentFound(lineCounter, start, end-start, str, isBold, isNum, isSmall);
                    line.append(str);
                }

                start=i+1;
                end=start;
                onCmd=true;
            }
            else if(i==text.length()-1){
                if(end-start<0)continue;
                String str=text.substring(start, end);
                listener.onSegmentFound(lineCounter, start, end-start, str, isBold, isNum, isSmall);
                line.append(str);
            }
            else{
                end++;
            }
        }
        listener.onFinishParse(dotList);
    }

    public LinearIndex[] getHighlightMark(PLIndex pli){
        int index= pliToLinearIndex(pli.page, pli.line, pli.index);
        if(debug)System.out.println("Set highlight word at page "+pli.page+", index "+index+", length "+pli.length);
        String sample=getContentStr(pli.page,0,TO_END);
        if(debug)System.out.println("startIndex+length="+(index+pli.length)+", sample length="+sample.length());
        if(index+pli.length<sample.length()){
            if(debug)System.out.println("The highlight in one page");
            return new LinearIndex[]{new LinearIndex(pli.page, index, pli.length)};
        }
        else{
            if(debug)System.out.println("The highlight words over second page");
            return new LinearIndex[]{
                    new LinearIndex(pli.page, index, sample.length()-index),
                    new LinearIndex(pli.page+1, 0, pli.length-(sample.length()-index))};
            /*
            highlightWord[0][0]=startPage;
            highlightWord[0][1]=index;
            highlightWord[0][2]=sample.length()-index;
            highlightWord[1][0]=startPage+1;
            highlightWord[1][1]=0;
            highlightWord[1][2]=length-(sample.length()-index);
            Log.d(getClass().getName(),"Set highlight at page,line,word: "+highlightWord[0][0]+", "+highlightWord[0][1]+", "+highlightWord[0][2]+" and "+highlightWord[1][0]+", "+highlightWord[1][1]+", "+highlightWord[1][2]);
            */
        }
    }

    public PLIndex searchLast(PLIndex plIndex, String str){
        return searchLast(plIndex.page, plIndex.line, plIndex.index, str);
    }
    /*
    * 從 startPage 頁的第 startLine 行的第 startWord 位置開始「向前」搜尋 str 的字串內容，回傳資料整數陣列亦代表{頁數, 行數, 字數}。
    * */
    public PLIndex searchLast(int startPage, int startLine, int startWord, String str){
        if(debug)System.out.println("search last "+str+" from page "+startPage+", line "+startLine+", word "+startWord);

        int rangePageStart=startPage;
        int rangePageEnd=startPage;
        int pageLen[][]=new int[5][2];
        int pageIndex=0;

        // if startWord == -1 that mean the search is from end to start
        if(startWord<0)startLine--;
        if(startLine<0)startPage--;
        if(startPage<0)return null;

        if(debug)System.out.println("Call pliToLinearIndex(page="+startPage+", line="+startLine+", word="+startWord+")");
        int startIndex= pliToLinearIndex(startPage, startLine, startWord);

        String sample=getContentStr(startPage, startIndex, TO_START);
        pageLen[0][0]=startPage;
        pageLen[0][1]=sample.length();
        //while(rangePageEnd<TheoryData.content.length-1){
        while(rangePageEnd >= 0){  // Make sure the index not over page range.
            while(sample.length()<str.length()){  // Make sure the content longer then searching string.
                ++pageIndex;
                if(--rangePageEnd<0)return null;  // The rest length of content not longer then searching string.

                sample=getContentStr(rangePageEnd,0,TO_END)+sample;
                pageLen[pageIndex][0]=rangePageEnd;
                pageLen[pageIndex][1]=sample.length();
                if(debug)System.out.println("Add page "+rangePageEnd+" to sample");
            }

            int searchResult=searchString(sample,str,TO_START);
            if(searchResult==-1){
                rangePageStart=rangePageEnd;
                //int len=sample.length()-str.length()+1;
                sample=sample.substring(0,str.length()-1);
                pageIndex=0;
                pageLen[0][0]=rangePageStart;
                pageLen[0][1]=str.length()-1;
                if(debug)System.out.println("Not found, sample ="+sample);
            }
            else{
                if(debug)System.out.println("Found at "+searchResult+" of "+sample);
                for(int i=0;i<=pageIndex;i++){
                    if(searchResult<pageLen[i][1]){
                        int index=0;
                        if(i==0){
                            String pageContent=getContentStr(pageLen[i][0], 0,TO_END);
                            index=searchResult-str.length()+1;
                            if(debug)System.out.println("Page content length="+pageContent.length()+", Start index = "+index);
                        }
                        else {
                            index=searchResult-pageLen[i-1][1];
                            if(debug)System.out.println("searchResult("+searchResult+") - page index["+(i-1)+"][1]("+pageLen[i-1][1]+") = "+index);
                        }

                        PLIndex ret= linearToPLIndex(pageLen[i][0], index);
                        if(debug)System.out.println("Found at page="+ret.page+", line="+ret.line+", word="+ret.index);
                        return ret;
                    }
                }
            }
        }

        return null;
    }

    public PLIndex searchNext(PLIndex plIndex, String str){
        return searchNext(plIndex.page, bookContent.length-1, plIndex.line, plIndex.index, str);
    }
    /*
    * 從 startPage 頁的第 startLine 行的第 startWord 位置開始「向後」搜尋 str 的字串內容，回傳資料整數陣列亦代表{頁數, 行數, 字數}。
    * */
    public PLIndex searchNext(int startPage, int startLine, int startWord, String str){
        return searchNext(startPage, bookContent.length-1, startLine, startWord, str);
    }
    /*
    * 從 startPage 頁的第 startLine 行的第 startWord 位置開始「向後」搜尋 str 的字串內容，直到搜尋到endPage頁為止，回傳資料整數陣列亦代表{頁數, 行數, 字數}。
    * */
    public PLIndex searchNext(int startPage, int endPage, int startLine, int startWord, String str){
        if(debug)System.out.println("search next"+str+" from page "+startPage+", line "+startLine+", word "+startWord);
        int rangePageStart=startPage;
        int rangePageEnd=startPage;
        int pageLen[][]=new int[5][2];
        int pageIndex=0;
        int startIndex= pliToLinearIndex(startPage, startLine, startWord);

        String sample=getContentStr(startPage, startIndex, TO_END);
        pageLen[0][0]=startPage;
        pageLen[0][1]=sample.length();
        while(rangePageEnd<endPage){
            while(sample.length()<str.length()){
                if(debug) System.out.println("Sample="+sample+"sample length="+sample.length()+", str length="+str.length());
                ++pageIndex;
                if(++rangePageEnd>=bookContent.length)return null;  // The rest length of content not longer then searching string.

                sample+=getContentStr(rangePageEnd,0,TO_END);
                pageLen[pageIndex][0]=rangePageEnd;
                pageLen[pageIndex][1]=sample.length();
                if(debug)System.out.println("Add page "+rangePageEnd+" to sample");
            }

            int searchResult=searchString(sample,str,TO_END);
            if(searchResult==-1){
                rangePageStart=rangePageEnd;
                int len=sample.length()-str.length()+1;
                sample=sample.substring(len);
                pageIndex=0;
                pageLen[0][0]=rangePageStart;
                pageLen[0][1]=sample.length();
                if(debug)System.out.println("Not found, sample ="+sample);
            }
            else{
                if(debug)System.out.println("Found at "+searchResult+" of "+sample);
                for(int i=0;i<=pageIndex;i++){
                    if(searchResult<pageLen[i][1]){

                        int index=0;
                        if(i==0){
                            String pageContent=getContentStr(pageLen[i][0], 0,TO_END);
                            index=pageContent.length()-pageLen[i][1]+searchResult;
                            if(debug)System.out.println("Page content length="+pageContent.length()+", Start index = "+index);
                        }
                        else {
                            index=searchResult-pageLen[i-1][1];
                            if(debug)System.out.println("searchResult("+searchResult+") - page index["+(i-1)+"][1]("+pageLen[i-1][1]+") = "+index);
                        }

                        PLIndex ret= linearToPLIndex(pageLen[i][0],index);
                        if(debug)System.out.println("Found at page="+ret.page+", line="+ret.line+", word="+ret.index);
                        return ret;
                    }
                }
            }
        }

        return null;
    }

    /*
	 * Search the str from sample string that return the index of sample where exist the same string with str.
	 * The direct must be MyListView.TO_START(from 0 to fromIndex) or MyListView.TO_END(from fromIndex to end of content).
	 * */
    private int searchString(String sample, String str, int direct){
        if(debug)System.out.println("The direct is "+((direct==TO_START)?"TO_START":"TO_END"));
        int shift=0;

        if(direct == TO_START){
            for(int i=sample.length()-1;i>=0;i--){
                shift=0;
                if(sample.charAt(i)=='\n')continue;
                for(int j=0;j<str.length();j++){
                    while(true){	// Drop '\n'
                        if((i-j-shift)<0)
                            return -1;
                        if(sample.charAt(i-j-shift) == '\n'){
                            shift++;
                            continue;
                        }
                        break;
                    }
                    // System.out.print("Compare: "+sample.charAt(i-j-shift)+" and "+str.charAt(str.length()-1-j));
                    if(sample.charAt(i-j-shift) != str.charAt(str.length()-1-j)){
                        if(debug) System.out.println();
                        break;
                    }
                    if(j==str.length()-1){
                        if(debug)System.out.println(" match!");
                        return i-shift;
                    }
                }
            }

            return -1;
        }

        // TO_END

        for(int i=0;i<sample.length()-str.length()+1;i++){
            shift=0;
            if(sample.charAt(i)=='\n')continue;
            for(int j=0;j<str.length();j++){
                while(true){	// Drop '\n'
                    if((i+j+shift)>=sample.length())
                        return -1;
                    if(sample.charAt(i+j+shift) == '\n'){
                        shift++;
                        continue;
                    }
                    break;
                }

                //System.out.print("Compare: "+sample.charAt(i+j+shift)+" and "+str.charAt(j));

                if(sample.charAt(i+j+shift) != str.charAt(j)){
                    if(debug)System.out.println();
                    break;
                }
                if(j==str.length()-1){
                    if(debug)System.out.println(" match!");
                    return i;
                }
            }
        }
        return -1;
    }

    public int pliToLinearIndex(PLIndex plIndex){
        return pliToLinearIndex(plIndex.page, plIndex.line, plIndex.index);
    }
    /*
	 * Convert the PAGE,LINE,WORD index to linear index, if word == -1, that mean end of the LINE of PAGE, if line == -1, that mean end of the page.
	 * The index exclude "<b>","</b>","<n>", "</n>", "<s>", "</s>" but include '\n'.
	 * */
    public int pliToLinearIndex(int page, int lineIndex, int word){
        String sample=getContentStr(page, 0,TO_END);
        String line[]=sample.split("\n");
        int len[]=new int[line.length];

        for(int i=0;i<line.length;i++){
            if(i==0)len[0]=line[0].length()+1;
            else len[i]=line[i].length()+len[i-1]+1;
        }
        len[line.length-1]--;

        // Check is the word index or lineIndex has become -1.
        if(lineIndex==-1)return len[line.length-1];
        if(word==-1)return len[lineIndex];

        // Debug
        for(int i=0;i<line.length;i++)
            if(debug) System.out.println(line[i]+"\n"+len[i]);

        int index=-1;
        if(lineIndex==0)index = word;
        else index = len[lineIndex-1]+word;

        try{
            if(debug)System.out.println("Exam the index("+index+") of word = "+sample.charAt(index));
        }catch(Exception e){e.printStackTrace();}
        return index;
    }

    public PLIndex linearToPLIndex(LinearIndex linearIndex){
        return linearToPLIndex(linearIndex.page, linearIndex.index);
    }
    /*
     * Convert the linear index to PAGE,LINE,WORD index, if word == -1, that mean end of the LINE of PAGE, if line == -1, that mean end of the page.
     * The index exclude "<b>","</b>","<n>", "</n>", "<s>", "</s>" but include '\n'.
     * */
    public PLIndex linearToPLIndex(int page, int index){
        if(debug) System.out.println("Convert page "+page+" index "+ index +" to line, word");
        String sample=getContentStr(page, 0,TO_END);
        String line[]=sample.split("\n");
        int len[]=new int[line.length];

        for(int i=0;i<line.length;i++){
            if(i==0)len[0]=line[0].length()+1;
            else len[i]=line[i].length()+len[i-1]+1;
        }
        len[line.length-1]--;

        // Debug
        for(int i=0;i<line.length;i++){
            if(debug) System.out.println(line[i]+"\n"+len[i]);
        }

        int lineIndex=-1, word=-1;
        for(int i=0;i<line.length;i++){
            if(index < len[i]){
                if(i==0){
                    lineIndex=0;
                    word=index;
                    if(debug)
                        if(word==line[lineIndex].length())System.out.println("Exam the converted string \\n");
                        else System.out.println("Exam the converted string "+line[lineIndex].charAt(word));
                    break;
                }
                else{
                    lineIndex=i;

                    word=index-len[i-1];
                    if(debug)
                        if(word==line[lineIndex].length())System.out.println("Exam the converted string \\n");
                        else System.out.println("Exam the converted string "+line[lineIndex].charAt(word));
                    break;
                }
            }
        }

        PLIndex ret=new PLIndex(page, lineIndex, word);
        if(debug)System.out.println("Result page "+page+" line "+ lineIndex +" word "+word);
        return ret;
    }

    /*
     * 從 startPage 頁的 fromIndex 位置開始取出有效字元，以 direct 方向決定向前取或向後取。所謂有效字元即不包含 "<b>","</b>","<n>", "</n>", "<s>", "</s>" 等標籤字元，但包含換行[\n]符號。
     *
     * Get content of page of theory data, the data exclude "<b>","</b>","<n>", "</n>", "<s>", "</s>" but include '\n'.
     * The direct must be MyListView.TO_START(from 0 to fromIndex) or MyListView.TO_END(from fromIndex to end of content).
     * */
    public String getContentStr(int startPage, int fromIndex, int direct){
        if(debug)System.out.println("Get content string page: "+startPage+", startWord: "+fromIndex);
        String page=bookContent[startPage].replace("‧", "").replace("。", "");
        page=page.replace("<b>", "");
        page=page.replace("</b>", "");
        page=page.replace("<n>", "");
        page=page.replace("</n>", "");
        page=page.replace("<s>", "");
        page=page.replace("</s>", "");

        String result=null;
        if(direct == TO_START)result=page.substring(0,fromIndex);
        else result=page.substring(fromIndex);

        return result;
    }
}
