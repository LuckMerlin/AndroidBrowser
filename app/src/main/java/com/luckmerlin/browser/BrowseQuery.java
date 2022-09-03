package com.luckmerlin.browser;

import com.luckmerlin.browser.file.File;

public class BrowseQuery {
    public final File mFolder;
    public final String mSearchInput;

    public BrowseQuery(File folder, String searchInput){
        mFolder=folder;
        mSearchInput=searchInput;
    }

}
