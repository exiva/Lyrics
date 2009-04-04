package net.exiva.lyrics;

import danger.app.Application;
import danger.app.Event;
import danger.app.Resource;
import danger.ui.*;
import danger.util.DEBUG;

public class lyricsView extends ScreenWindow implements Resources, Commands {
	private static TextField artist;
	private static EditText song;
	private static EditText lyricsField;
	private static Button searchButton;
	
	public lyricsView() {}

	public static lyricsView create() {
		lyricsView me = (lyricsView) Application.getCurrentApp().getResources().getScreen(ID_MAIN_SCREEN, null);
		return me;
	}

	public final void adjustActionMenuState(Menu menu)
	{
		menu.removeAllItems();
		menu.addFromResource(Application.getCurrentApp().getResources(), ID_MAIN_MENU, this);
    }

	public void about() 
	{
		AlertWindow about = getApplication().getAlert(ID_ABOUT, this);
		about.show();
	}

	public void help() 
	{
		DialogWindow help = getApplication().getDialog(helpDialog, this);
		help.show();
	}

	public void setLyrics(String lyrics)
	{
		lyricsField = (EditText) getChildWithID(LYRIC_TEXT);
        lyricsField.setText(lyrics);
		setFocusedChild(lyricsField);
	}

	public void setTrackInfo(String inartist, String intitle)
	{
		artist = (TextField) getChildWithID(ARTIST_FIELD);
		song = (TextField) getChildWithID(SONG_FIELD);
		artist.setText(inartist.toString());
		song.setText(intitle.toString());
	}

	public boolean receiveEvent(Event e) {
		switch (e.type) {
			case EVENT_SEARCH:
			{
				artist = (TextField) getChildWithID(ARTIST_FIELD);
				song = (TextField) getChildWithID(SONG_FIELD);
				lyricsField = (EditText) getChildWithID(LYRIC_TEXT);
				searchButton = (Button) getChildWithID(SEARCH_BUTTON);
				if (!"".equals(artist.toString())) {
		        	lyricsField.setText("Searching...");
					lyrics.searchLyrics(artist.toString(),song.toString());
					setFocusedChild(searchButton);
				}
				else {
		        	lyricsField.setText("Artist field was left empty, cannot search.");					
				}
				return true;
			}
			case EVENT_UPDATE:
			{
				lyrics.getInfo();
				return true;
			}
			case EVENT_EMAIL:
			{
				artist = (TextField) getChildWithID(ARTIST_FIELD);
				song = (TextField) getChildWithID(SONG_FIELD);
				lyrics.sendMail(artist.toString(), song.toString());
				return true;
			}
			case EVENT_HELP:
			{
				help();
				return true;
			}
			case EVENT_ABOUT:
			{
				about();
				return true;
			}
			default:
			break;
		}
		return super.receiveEvent(e);
	}

    public boolean eventWidgetUp(int inWidget, Event e) {
		switch (inWidget) {
			case Event.DEVICE_BUTTON_CANCEL:
			Application.getCurrentApp().returnToLauncher();
			return true;
			case Event.DEVICE_BUTTON_BACK:
			Application.getCurrentApp().returnToLauncher();
			return true;
		}
		return super.eventWidgetUp(inWidget, e);
	}
}