package in.nashapp.epublibdroid;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

/**
 * Created by Avinash on 25-05-2017.
 */

public class EpubReaderView extends WebView {
        public Book book;
        public ArrayList<Chapter> ChapterList = new ArrayList<Chapter>();
        private int ChapterNumber=-1;
        private float Progress;
        private int page_number;
        private float touchX;
        private float touchY;
        private String ResourceLocation="";
        private Context context;
        private android.view.ActionMode mActionMode = null;
        private SelectActionModeCallback actionModeCallback;
        private String seletedText ="";
        private boolean loading = false;
        private EpubReaderListener listener;
        public int THEME_LIGHT = 1;
        public int THEME_DARK = 2;
        public int METHOD_HIGHLIGHT = 1;
        public int METHOD_UNDERLINE = 2;
        public int METHOD_STRIKETHROUGH = 3;

        private int current_theme=1;
        //private CustomActionModeCallback mActionModeCallback;
        public interface EpubReaderListener {
            void OnPageChangeListener(int ChapterNumber,float Progress);//Working
            void OnChapterChangeListener(int ChapterNumber);//Working
            void OnTextSelectionModeChangeListner(Boolean mode);//Working
            void OnLinkClicked(String url);//Working
            void OnBookStartReached();//Working
            void OnBookEndReached();//Working
        }
    public void setEpubReaderListener(EpubReaderListener listener) {
        this.listener = listener;
    }
    public EpubReaderView(Context context) {
        super(context);
        init(context);
    }
    private class Chapter{
        String name;
        String content;
        public Chapter(String name, String content) {
            this.name = name;
            this.content = content;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getName() {
            return name;
        }
        public String getContent() {
            return content;
        }
    }

    public EpubReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public class SelectActionModeCallback implements android.view.ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mActionMode = mode;
            listener.OnTextSelectionModeChangeListner(true);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return true;
        }
        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            return true;
        }
        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            listener.OnTextSelectionModeChangeListner(false);
        }
    }
    @Override
    public android.view.ActionMode startActionMode(android.view.ActionMode.Callback callback) {
        ViewParent parent = getParent();
        if (parent == null) {
            return null;
        }
        actionModeCallback = new SelectActionModeCallback();
        return parent.startActionModeForChild(this, actionModeCallback);
    }
    private void init(Context context) {
        this.context = context;
        this.getSettings().setJavaScriptEnabled(true);
        WebSettings settings = this.getSettings();
        settings.setDefaultTextEncodingName("UTF-8");
        this.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float x = event.getRawX();
                        float y = event.getRawY();
                        if (touchX - x > ConvertIntoPixel(100)) {
                            NextPage();
                        } else if (x - touchX > ConvertIntoPixel(100)) {
                            PreviousPage();
                        }
                        else if (touchY - y  > ConvertIntoPixel(100)) {
                            NextPage();
                        }
                        else if (y - touchY > ConvertIntoPixel(100)) {
                            PreviousPage();
                        }
                        break;
                }
                return false;
            }
        });
    }
    public int GetTheme(){
        return current_theme;
    }

    private void ProcessJavascript(String js,String callbackFunction){
        Log.d("EpubReader",callbackFunction+" Called");
        if (Build.VERSION.SDK_INT > 19) {
            this.evaluateJavascript("(function(){"+js+"})()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {}
            });
        } else {
            this.loadUrl("javascript:js."+callbackFunction+"("+js+")");
        }
    }
    public void SetTheme(int theme){
        if(theme==THEME_LIGHT) {
            current_theme = THEME_LIGHT;
            ProcessJavascript("var elements = document.getElementsByTagName('*');\n" +
                "for (var i = 0; i < elements.length; i++) {\n" +
                " if(elements[i].tagName!=\"SPAN\")\n" +
                "  elements[i].style.backgroundColor='white';\n" +
                " elements[i].style.color='black';\n" +
                "}","changeTheme");
        }else{
            current_theme = THEME_DARK;
            ProcessJavascript("var elements = document.getElementsByTagName('*');\n" +
                "for (var i = 0; i < elements.length; i++) {\n" +
                " if(elements[i].tagName!=\"SPAN\")\n" +
                "  elements[i].style.backgroundColor='black';\n" +
                "elements[i].style.color='white';\n" +
                "}","changeTheme");
        }
    }
    public void Annotate(String jsonData,final int selectionMethod,String hashcolor) {
                String js = "\tvar data = JSON.parse("+jsonData+");\n" +
                        "\tvar selectedText = data['selectedText'];\n" +
                        "\tvar startOffset = data['startOffset'];\n" +
                        "\tvar endOffset = data['endOffset'];\n" +
                        "\tvar startNodeData = data['startNodeData'];\n" +
                        "\tvar startNodeHTML = data['startNodeHTML'];\n" +
                        "\tvar startNodeTagName = data['startNodeTagName'];\n" +
                        "\tvar endNodeData = data['endNodeData'];\n" +
                        "\tvar endNodeHTML = data['endNodeHTML'];\n" +
                        "\tvar endNodeTagName = data['endNodeTagName'];\n" +
                        "    var tagList = document.getElementsByTagName(startNodeTagName);\n" +
                        "    for (var i = 0; i < tagList.length; i++) {\n" +
                        "        if (tagList[i].innerHTML == startNodeHTML) {\n" +
                        "            var startFoundEle = tagList[i];\n" +
                        "        }\n" +
                        "    }\n" +
                        "\tvar nodeList = startFoundEle.childNodes;\n" +
                        "    for (var i = 0; i < nodeList.length; i++) {\n" +
                        "        if (nodeList[i].data == startNodeData) {\n" +
                        "            var startNode = nodeList[i];\n" +
                        "        }\n" +
                        "    }\n" +
                        "\tvar tagList = document.getElementsByTagName(endNodeTagName);\n" +
                        "    for (var i = 0; i < tagList.length; i++) {\n" +
                        "        if (tagList[i].innerHTML == endNodeHTML) {\n" +
                        "            var endFoundEle = tagList[i];\n" +
                        "        }\n" +
                        "    }\n" +
                        "    var nodeList = endFoundEle.childNodes;\n" +
                        "    for (var i = 0; i < nodeList.length; i++) {\n" +
                        "        if (nodeList[i].data == endNodeData) {\n" +
                        "            var endNode = nodeList[i];\n" +
                        "        }\n" +
                        "    }\n" +
                        "    var range = document.createRange();\n" +
                        "\trange.setStart(startNode, startOffset);\n" +
                        "    range.setEnd(endNode, endOffset);\n" +
                        "    var sel = window.getSelection();\n" +
                        "\tsel.removeAllRanges();\n" +
                        "\tdocument.designMode = \"on\";\n" +
                        "\tsel.addRange(range);\n";
                        if(selectionMethod == METHOD_HIGHLIGHT)
                            js = js + "\tdocument.execCommand(\"HiliteColor\", false, \""+hashcolor+"\");\n";
                        if(selectionMethod == METHOD_UNDERLINE)
                            js = js + "\tdocument.execCommand(\"underline\");\n";
                        if(selectionMethod == METHOD_STRIKETHROUGH)
                            js = js + "\tdocument.execCommand(\"strikeThrough\");\n";
                        js = js+"\tsel.removeAllRanges();\n" +
                                "\tdocument.designMode = \"off\";\n" +
                                "\treturn \"{\\\"status\\\":1}\";\n";
                ProcessJavascript(js,"annotate");
                Log.d("EpubReader","AnnotateCalled");
    }
    public void ExitSelectionMode(){
        mActionMode.finish();
        String js = "window.getSelection().removeAllRanges();";
        ProcessJavascript(js,"deselect");
    }
    public void ProcessTextSelection() {
        String js = "\tvar sel = window.getSelection();\n" +
                "\tvar jsonData ={};\n" +
                "\tif(!sel.isCollapsed) {\n" +
                "\t\tvar range = sel.getRangeAt(0);\n" +
                "\t\tstartNode = range.startContainer;\n" +
                "\t\tendNode = range.endContainer;\n" +
                "\t\tjsonData['selectedText'] = range.toString();\n" +
                "\t\tjsonData['startOffset'] = range.startOffset;  // where the range starts\n" +
                "\t\tjsonData['endOffset'] = range.endOffset;      // where the range ends\n" +
                "\t\tjsonData['startNodeData'] = startNode.data;                       // the actual selected text\n" +
                "\t\tjsonData['startNodeHTML'] = startNode.parentElement.innerHTML;    // parent element innerHTML\n" +
                "\t\tjsonData['startNodeTagName'] = startNode.parentElement.tagName;   // parent element tag name\n" +
                "\t\tjsonData['endNodeData'] = endNode.data;                       // the actual selected text\n" +
                "\t\tjsonData['endNodeHTML'] = endNode.parentElement.innerHTML;    // parent element innerHTML\n" +
                "\t\tjsonData['endNodeTagName'] = endNode.parentElement.tagName;   // parent element tag name\n" +
                "\t\tjsonData['status'] = 1;\n" +
                "\t}else{\n" +
                "\t\tjsonData['status'] = 0;\n" +
                "\t}\n" +
                "\treturn (JSON.stringify(jsonData));";
        if (Build.VERSION.SDK_INT > 19) {
            this.evaluateJavascript("(function(){"+js+"})()",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.v("EpubReader", "SELECTION>19:" + value);
                            Log.v("EpubReader", "SELECTION_P>19:" +  value.substring(1,value.length()-1).replaceAll("\\\\\"","\""));
                            Log.v("EpubReader", "SELECTION_P>19:" +  value.substring(1,value.length()-1).replaceAll("\\\\\"","\"").replaceAll("\\\\\\\\\"","\\\\\"").replaceAll("\\\\\\\"","\\\\\"").replaceAll("\\\\\\\\\\\"","\\\\\""));
                            String text ="";
                            try {
                                String parse_json = value.substring(1,value.length()-1).replaceAll("\\\\\"","\"").replaceAll("\\\\\\\\\"","\\\\\"").replaceAll("\\\\\\\"","\\\\\"").replaceAll("\\\\\\\\\\\"","\\\\\"");
                                JSONObject object = new JSONObject(parse_json);
                                text = object.getString("selectedText");
                            }catch(Exception e){e.printStackTrace();}
                            JSONObject selectedTextJson = new JSONObject();
                            try {
                                selectedTextJson.put("DataString",value);
                                selectedTextJson.put("ChapterNumber",ChapterNumber);
                                selectedTextJson.put("SelectedText",text);
                            }catch(Exception e){seletedText="";}
                            seletedText = selectedTextJson.toString();
                        }
                    });
        } else {
            this.loadUrl("javascript:js.selection("+js+")");
            this.addJavascriptInterface(new JavaScriptInterface(), "js");
        }
    }
    public String getSelectedText() {
        return seletedText;
    }
    public class JavaScriptInterface
    {
        @JavascriptInterface
        public void selection(String value){
            Log.v("EpubReader", "SelectText<=19:" + value);
            String text ="";
            try {
                String parse_json = value.substring(1,value.length()-1).replaceAll("\\\\\\\"","\\\"").replaceAll("\\\"","\"");
                JSONObject object = new JSONObject(parse_json);
                text = object.getString("selectedText");
            }catch(Exception e){e.printStackTrace();}
            JSONObject selectedTextJson = new JSONObject();
            try {
                selectedTextJson.put("DataString",value);
                selectedTextJson.put("ChapterNumber",ChapterNumber);
                selectedTextJson.put("SelectedText",text);
            }catch(Exception e){seletedText="";}
            seletedText = selectedTextJson.toString();
        }
        @JavascriptInterface
        public void annotate()
        {
            Log.v("EpubReader","annotate<=19");
        }
        @JavascriptInterface
        public void deselect()
        {
            Log.v("EpubReader","Deselect<=19");
        }
    }
    private void DownloadResource(String directory) {
        //Log.d("epubResourcePath", directory);
        try {
            nl.siegmann.epublib.domain.Resources rst = book.getResources();
            Collection<Resource> clrst = rst.getAll();
            Iterator<Resource> itr = clrst.iterator();
            while (itr.hasNext()) {
                Resource rs = itr.next();
                if ((rs.getMediaType() == MediatypeService.JPG) || (rs.getMediaType() == MediatypeService.PNG) || (rs.getMediaType() == MediatypeService.GIF) || rs.getMediaType() == MediatypeService.CSS)  {
                    File oppath1 = new File(directory+File.separator+rs.getHref());
                    //Log.d("EpubReaderRD", rs.getHref()+"\t"+oppath1.getAbsolutePath()+"\t"+rs.getSize());
                    File dir = new File(oppath1.getAbsolutePath().substring(0,oppath1.getAbsolutePath().lastIndexOf("/")));
                    if(!dir.exists())
                        dir.mkdirs();
                    oppath1.createNewFile();
                    FileOutputStream fos1 = new FileOutputStream(oppath1);
                    fos1.write(rs.getData());
                    fos1.close();
                    //Log.d("EpubReaderFileE",oppath1.getAbsoluteFile()+" "+oppath1.exists());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("error", e.getMessage());
        }
    }
    public void OpenEpubFile(String epub_location) {
        try {
            InputStream epubInputStream = new BufferedInputStream(new FileInputStream(epub_location));
            this.book = (new EpubReader()).readEpub(epubInputStream);
            String epub_temp_extraction_location = context.getCacheDir() + "/tempfiles";
            deleteFiles(new File(epub_temp_extraction_location));
            if (!new File(epub_temp_extraction_location).exists())
                new File(epub_temp_extraction_location).mkdirs();
            try {
                DownloadResource(epub_temp_extraction_location);
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
            }
            File dir1 = new File(epub_temp_extraction_location + File.separator + "OEBPS");
            String resource_folder = book.getOpfResource().getHref().replace("content.opf","").replace("/","");
            File dir2 = new File(epub_temp_extraction_location + File.separator + resource_folder);
            if (dir1.exists() && dir1.isDirectory()) {
                ResourceLocation = "file://" + epub_temp_extraction_location + File.separator + "OEBPS" + File.separator;
            }else if(dir2.exists() && dir2.isDirectory()&&!resource_folder.equals("")){
                ResourceLocation = "file://" + epub_temp_extraction_location + File.separator + resource_folder + File.separator;
            }else {
                ResourceLocation = "file://" + epub_temp_extraction_location + File.separator;
            }
            Log.d("EpubReaderRL",ResourceLocation);
            if(ResourceLocation.contains("OEPBS")&&book.getTableOfContents().getTocReferences().size()>1)
                ProcessChaptersByTOC(book.getTableOfContents().getTocReferences());
            else if(book.getTableOfContents().getTocReferences().size()>1){
                ProcessChaptersByTOC(book.getTableOfContents().getTocReferences());
            }else
                ProcessChaptersBySpline(book.getSpine());
        }catch(Exception e){}
    }
    private static void deleteFiles (File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();   //All files and sub folders
            for(int x=0; files != null && x<files.length; x++)
                deleteFiles(files[x]);
            file.delete();
        }
        else
            file.delete();
    }
    private void ProcessChaptersByTOC(List<TOCReference> tocReferences) {
        if(tocReferences.size()>0){
            for (TOCReference TOC : tocReferences) {
                StringBuilder builder = new StringBuilder();
                try{BufferedReader r = new BufferedReader(new InputStreamReader(TOC.getResource().getInputStream()));
                    String aux = "";
                    while ((aux = r.readLine()) != null) {
                        builder.append(aux);
                    }
                }catch(Exception e){}
                ChapterList.add(new Chapter(TOC.getTitle(),builder.toString()));
                if(TOC.getChildren().size()>0){
                    ProcessChaptersByTOC(TOC.getChildren());
                }
            }
        }
    }
    private void ProcessChaptersBySpline(Spine spine) {
        int ChapterNumber=1;
        if(spine!=null){
            for(int i=0;i<spine.size();i++){
                StringBuilder builder = new StringBuilder();
                try{BufferedReader r = new BufferedReader(new InputStreamReader(spine.getResource(i).getInputStream()));
                    String aux = "";
                    while ((aux = r.readLine()) != null) {
                        builder.append(aux);
                    }
                }catch(Exception e){e.printStackTrace();}
                ChapterList.add(new Chapter((spine.getResource(i).getTitle()!=null?spine.getResource(i).getTitle():ChapterNumber+""),builder.toString()));
                //Log.d("EpubReaderContent",builder.toString());
                ChapterNumber++;
            }
        }else{
            Log.d("EpubReader","spline is null");
        }

    }

    public void GotoPosition(int ChapterNumber,final float Progress){
        Log.d("EpubReader","Chap"+ChapterNumber+" "+this.ChapterNumber);
        if(ChapterNumber!=this.ChapterNumber) {
            if(ChapterNumber<0){
                this.ChapterNumber = 0;
                this.Progress = 0;
            }else if(ChapterNumber>=ChapterList.size()){
                this.ChapterNumber = ChapterList.size()-1;
                this.Progress = 1;
            }else{
                this.ChapterNumber = ChapterNumber;
                this.Progress = Progress;
            }
            this.loadDataWithBaseURL(ResourceLocation, ChapterList.get(this.ChapterNumber).getContent().replaceAll("href=\"http", "hreflink=\"http").replaceAll("<a href=\"[^\"]*", "<a ").replaceAll("hreflink=\"http", "href=\"http"), "text/html", "utf-8", null);
            this.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    SetTheme(current_theme);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        int TotalHeight =EpubReaderView.this.GetTotalContentHeight();
                        Log.d("EpubReaderChap",(int)(TotalHeight*Progress)+" "+TotalHeight+" "+Progress);
                        EpubReaderView.this.scrollTo(0,(int)(TotalHeight*Progress));
                        }
                    },500);
                }
                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    listener.OnLinkClicked(url);
                    return true;
                }
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    listener.OnLinkClicked(request.getUrl().toString());
                    return true;
                }
            });
        }
    }
    public void ListChaptersDialog(int theme){
        try {
            ArrayList<String> ChapterListString = new ArrayList<String>();
            for (int i=0;i<ChapterList.size();i++) {
                ChapterListString.add(ChapterList.get(i).getName());
            }
            final String[] items = ChapterListString.toArray(new String[ChapterListString.size()]);
            AlertDialog.Builder alertbuilder;
            if(theme==this.THEME_DARK)
                alertbuilder = new AlertDialog.Builder(context,R.style.DarkDialog);
            else
                alertbuilder = new AlertDialog.Builder(context,R.style.LightDialog);
            alertbuilder.setTitle("Select the Chapter");
            alertbuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                GotoPosition(item,0);
                listener.OnChapterChangeListener(item);
                }
            });
            AlertDialog alert = alertbuilder.create();
            alert.show();
        }catch (Exception e){}
    }
    public void NextPage(){
        if(!loading) {
            int pageHeight = this.getHeight() - 50;
            int TotalHeight = GetTotalContentHeight();
            Log.d("epubPageNext", TotalHeight + "\t" + this.getScrollY() + "\t" + pageHeight);
            if (TotalHeight > this.getScrollY() + this.getHeight()) {
                loading = true;
                Progress = (float) (this.getScrollY() + pageHeight) / TotalHeight;
                page_number = (int) ((this.getScrollY() + pageHeight) / pageHeight);
                //this.scrollTo(0, page_number * pageHeight);
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollY",
                        (page_number - 1) * pageHeight, page_number * pageHeight);
                anim.setDuration(400);
                anim.start();
                listener.OnPageChangeListener(ChapterNumber, Progress);
                Log.d("EpubReaderProgress", Progress + " " + pageHeight + " " + this.getScrollY() + " " + TotalHeight);
                loading = false;
            } else {
                NextChapter();
            }
        }
    }
    public void PreviousPage(){
        if(!loading) {
            int pageHeight = this.getHeight() - 50;
            int TotalHeight = GetTotalContentHeight();
            Log.d("epubPagePre", this.getScrollY() + "\t" + pageHeight);
            if (this.getScrollY() - pageHeight >= 0) {
                loading = true;
                Progress = (float) (this.getScrollY() - pageHeight) / TotalHeight;
                Log.d("EpubReaderProgress", Progress + " " + pageHeight + " " + this.getScrollY() + " " + TotalHeight);
                page_number = ((int) ((this.getScrollY() - pageHeight) / pageHeight));
                //this.scrollTo(0, ((int) (page_number * pageHeight)));
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollY",
                        ((int) ((page_number + 1) * pageHeight)), ((int) (page_number * pageHeight)));
                anim.setDuration(400);
                anim.start();
                listener.OnPageChangeListener(ChapterNumber, Progress);
                loading = false;
            } else if (this.getScrollY() > 0) {
                loading = true;
                Progress = 0;
                Log.d("EpubReaderProgress", Progress + " " + pageHeight + " " + this.getScrollY() + " " + TotalHeight);
                page_number = 0;
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollY",
                        ((int) ((page_number + 1) * pageHeight)), ((int) (page_number * pageHeight)));
                anim.setDuration(400);
                anim.start();
                listener.OnPageChangeListener(ChapterNumber, Progress);
                loading = false;
            } else {
                PreviousChapter();
            }
        }
    }
    public void NextChapter(){
        if(ChapterList.size()>ChapterNumber+1&&!loading){
            loading = true;
            GotoPosition(ChapterNumber+1,0);
            listener.OnChapterChangeListener(ChapterNumber);
            listener.OnPageChangeListener(ChapterNumber,Progress);
            loading = false;
        }else if(ChapterList.size()<=ChapterNumber+1){
            listener.OnBookEndReached();
        }
    }
    public void PreviousChapter(){
        if(ChapterNumber-1>=0&&!loading) {
            loading = true;
            GotoPosition(ChapterNumber-1, 1);
            listener.OnChapterChangeListener(ChapterNumber);
            listener.OnPageChangeListener(ChapterNumber,Progress);
            loading = false;
        }else if(ChapterNumber-1<0){
            listener.OnBookStartReached();
        }
    }
    public String GetChapterContent(){
        return ChapterList.get(ChapterNumber).getContent();
    }

    private int GetTotalContentHeight(){
        return (int) (this.getContentHeight() * getResources().getDisplayMetrics().density);
    }
    public float GetProgress(){ return Progress;
    }
    public int GetChapterNumber(){
        return ChapterNumber;
    }
    private int ConvertIntoPixel(int dp){
        Resources r = context.getResources();
        return  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    private void alertDialog(String title,String Message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(Message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    /*
    Public variables
    Book book; //epublib book object
    List<Chapter> ChapterList : Chapter has Name and Content
    int THEME_LIGHT = 1;
    int THEME_DARK = 2;

    EpubReaderListener
        void OnPageChangeListener(int ChapterNumber,float Progress);
        void OnChapterChangeListener(int ChapterNumber);
        void OnTextSelectionModeChangeListner(Boolean mode);
        void OnLinkClicked(String url);
        void OnBookStartReached();
        void OnBookEndReached();

    Public Functions
    int GetTheme()
    void SetTheme(int theme)//NightMode and DayMode
    void ProcessTextSelection()
    String getSelectedText()
    void Highlight(String jsonData,String hashcolor)
    void OpenEpubFile(String epub_location)
    void GotoPosition(int ChapterNumber,final float Progress)
    void ListChaptersDialog(int theme)
    void NextPage()
    void PreviousPage()
    void NextChapter()
    void PreviousChapter()
    String GetChapterContent()
    void ExitSelectionMode()
    float GetProgress()
    int GetChapterNumber()*/
}