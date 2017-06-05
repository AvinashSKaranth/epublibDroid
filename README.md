# epublibDroid
Android sdk for reading Epub using epublib (http://siegmann.nl/epublib)
[Sample App Code](https://github.com/AvinashSKaranth/epublibDroid/blob/master/app/src/main/java/in/nashapp/epublibdemo/ReaderActivity.java)

# public variables
```java
    Book book; //epublib book object#######
    List<Chapter> ChapterList : Chapter has Name and Content
    int THEME_LIGHT = 1;
    int THEME_DARK = 2;
```

# EpubReaderListener
```java
void OnPageChangeListener(int ChapterNumber,float Progress);
void OnChapterChangeListener(int ChapterNumber);
void OnTextSelectionModeChangeListner(Boolean mode);
void OnLinkClicked(String url);
void OnBookStartReached();
void OnBookEndReached();
```

# Public Functions
```java
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
int GetChapterNumber()
```
