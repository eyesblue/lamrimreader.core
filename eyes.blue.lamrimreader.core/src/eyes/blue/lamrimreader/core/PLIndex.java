package eyes.blue.lamrimreader.core;

/**
 * 代表頁(Page)、行(Line)、位置(Index)的物件
 * Created by father on 16/5/18.
 */
public class PLIndex {
    public int page=-1, line=-1, index=-1;
    public int length = -1;

    public PLIndex(int page, int line, int index){
        this.page=page;
        this.line=line;
        this.index=index;
    }

    public PLIndex(int page, int line, int index, int length){
        this(page,line,index);
        this.length=length;
    }

    @Override
    public boolean equals(Object obj){
        PLIndex pli=(PLIndex)obj;
        return (page == pli.page && line == pli.line && index == pli.index);
    }

    public void setLength(int length){
        this.length=length;
    }
}
