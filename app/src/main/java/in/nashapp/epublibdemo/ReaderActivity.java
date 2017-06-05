package in.nashapp.epublibdemo;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONObject;
import in.nashapp.epublibdroid.EpubReaderView;

public class ReaderActivity extends AppCompatActivity {
    EpubReaderView ePubReader;
    ImageView select_copy;
    ImageView select_highlight;
    ImageView select_underline;
    ImageView select_strikethrough;
    ImageView select_read;
    ImageView select_search;
    ImageView select_share;
    ImageView select_exit;
    ImageView read_aloud;
    ImageView show_toc;
    ImageView change_theme;
    LinearLayout bottom_contextual_bar;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        context = this;
        String epub_location = this.getIntent().getExtras().getString("epub_location");
        ePubReader = (EpubReaderView) findViewById(R.id.epub_reader);
        show_toc = (ImageView) findViewById(R.id.show_toc);
        read_aloud = (ImageView) findViewById(R.id.read_aloud);
        change_theme = (ImageView) findViewById(R.id.change_theme);
        bottom_contextual_bar = (LinearLayout) findViewById(R.id.bottom_contextual_bar);
        select_copy = (ImageView) findViewById(R.id.select_copy);
        select_highlight = (ImageView) findViewById(R.id.select_highlight);
        select_underline = (ImageView) findViewById(R.id.select_underline);
        select_strikethrough = (ImageView) findViewById(R.id.select_strikethrough);
        select_exit = (ImageView) findViewById(R.id.select_exit);
        select_read = (ImageView) findViewById(R.id.select_read);
        select_search = (ImageView) findViewById(R.id.select_search);
        select_share = (ImageView) findViewById(R.id.select_share);
        select_exit = (ImageView) findViewById(R.id.select_exit);
        ePubReader.OpenEpubFile(epub_location);
        ePubReader.GotoPosition(0,(float)0);
        ePubReader.setEpubReaderListener(new EpubReaderView.EpubReaderListener() {
            @Override
            public void OnTextSelectionModeChangeListner(Boolean mode) {
                Log.d("EpubReader","TextSelectionMode"+mode+" ");
                if(mode){
                    bottom_contextual_bar.setVisibility(View.VISIBLE);
                }else{
                    bottom_contextual_bar.setVisibility(View.GONE);
                }
            }
            @Override
            public void OnPageChangeListener(int ChapterNumber, float Progress) {
                Log.d("EpubReader","PageChange: Chapter:"+ChapterNumber+" Progress:"+Progress);
            }
            @Override
            public void OnChapterChangeListener(int ChapterNumber) {
                Log.d("EpubReader","ChapterChange"+ChapterNumber+" ");
            }
            @Override
            public void OnLinkClicked(String url) {
                Log.d("EpubReader","LinkClicked:"+url+" ");
            }
            @Override
            public void OnBookStartReached() {
                //Use this method to go to previous book
                //When user slides previous when opened the first page of the book
                Log.d("EpubReader","StartReached");
            }
            @Override
            public void OnBookEndReached() {
                //Use this method to go to next book
                //When user slides next when opened the last page of the book
                Log.d("EpubReader","EndReached");
            }
        });
        show_toc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ListChaptersDialog(ePubReader.GetTheme());
            }
        });
        read_aloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"TODO: Read Aloud : "+ePubReader.GetChapterContent(),Toast.LENGTH_LONG).show();
            }
        });
        change_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(ePubReader.GetTheme()==ePubReader.THEME_LIGHT) {
                   ePubReader.SetTheme(ePubReader.THEME_DARK);
               }else{
                   ePubReader.SetTheme(ePubReader.THEME_LIGHT);
               }
            }
        });
        select_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            Toast.makeText(context,"TODO: Read Aloud : "+SelectedText,Toast.LENGTH_LONG).show();
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            //Save ChapterNumber,DataString,Color,AnnotateMethod,BookLocation etc in database/Server to recreate highlight
                            if(ChapterNumber==ePubReader.GetChapterNumber())//Verify ChanpterNumber and BookLocation before suing highlight
                                ePubReader.Annotate(DataString,ePubReader.METHOD_HIGHLIGHT,"#ef9a9a");
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            //Save ChapterNumber,DataString,Color,BookLocation etc in database/Server to recreate highlight
                            Toast.makeText(context, "TODO:Selected highlight:" + SelectedText, Toast.LENGTH_LONG).show();
                            if(ChapterNumber==ePubReader.GetChapterNumber())//Verify ChanpterNumber and BookLocation before suing highlight
                                ePubReader.Annotate(DataString,ePubReader.METHOD_UNDERLINE,"#ef9a9a");
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            //Save ChapterNumber,DataString,Color,BookLocation etc in database/Server to recreate highlight
                            Toast.makeText(context, "TODO:Selected highlight:" + SelectedText, Toast.LENGTH_LONG).show();
                            if(ChapterNumber==ePubReader.GetChapterNumber())//Verify ChanpterNumber and BookLocation before suing highlight
                                ePubReader.Annotate(DataString,ePubReader.METHOD_STRIKETHROUGH,"#ef9a9a");
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(SelectedText);
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", SelectedText);
                                clipboard.setPrimaryClip(clip);
                            }
                            Toast.makeText(context,"Text Copied",Toast.LENGTH_LONG).show();
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            if(SelectedText.length()<120) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q="+SelectedText));
                                startActivity(browserIntent);
                            }else{
                                Toast.makeText(context,"Selected Text is too big to search",Toast.LENGTH_LONG).show();
                            }
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ProcessTextSelection();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String SelectedText = "";
                        int ChapterNumber = -1;
                        String DataString = "";
                        try {
                            JSONObject reponse = new JSONObject(ePubReader.getSelectedText());
                            SelectedText = reponse.getString("SelectedText");
                            ChapterNumber = reponse.getInt("ChapterNumber");
                            DataString = reponse.getString("DataString");
                        }catch(Exception e){e.printStackTrace();}
                        if(ChapterNumber>=0&&!SelectedText.equals("")&&!DataString.equals("")) {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Shared using epublibDroid");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, SelectedText);
                            startActivity(Intent.createChooser(sharingIntent,"Share using?"));
                        }
                        ePubReader.ExitSelectionMode();
                    }
                }, 100);
            }
        });
        select_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ePubReader.ExitSelectionMode();
            }
        });
    }
}
