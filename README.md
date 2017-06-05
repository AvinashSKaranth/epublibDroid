# epublibDroid
Android sdk for reading Epub using epublib (http://siegmann.nl/epublib)
[Sample App Code](https://github.com/AvinashSKaranth/epublibDroid/blob/master/app/src/main/java/in/nashapp/epublibdemo/ReaderActivity.java)

# Variables
```java
    Book book; //epublib book object
    List<Chapter> ChapterList : Chapter has Name and Content
    int THEME_LIGHT = 1;
    int THEME_DARK = 2;
```

# EpubReaderListener
```java
EpubReaderView ePubReader = new EpubReaderView(context);
ePubReader.setEpubReaderListener(new EpubReaderView.EpubReaderListener() {...}
void OnPageChangeListener(int ChapterNumber,float Progress);
void OnChapterChangeListener(int ChapterNumber);
void OnTextSelectionModeChangeListner(Boolean mode);
void OnLinkClicked(String url);
void OnBookStartReached();
void OnBookEndReached();
```

# Functions
```java
void OpenEpubFile(String epub_location)
void GotoPosition(int ChapterNumber,final float Progress)
void ListChaptersDialog(int theme)
void NextPage()
void PreviousPage()
void NextChapter()
void PreviousChapter()
int GetTheme() //1 is THEME_LIGHT , 2 is THEME_DARK 
void SetTheme(int theme)//1 is DayMode , 2 is NightMode
void ProcessTextSelection()
String getSelectedText()// Run after 100ms running ProcessTextSelection()
void Highlight(String jsonData,String hashcolor)
String GetChapterContent()
void ExitSelectionMode()
float GetProgress()
int GetChapterNumber()
```
