package com.kami.fileexplorer.ui.file;

import com.kami.fileexplorer.BasePresenter;
import com.kami.fileexplorer.BaseView;
import com.kami.fileexplorer.data.FileExplorer;

import java.util.List;

/**
 * author: youyi_sizuru
 * data: 2017/7/16
 */

interface FileContract {
    interface View extends BaseView<Presenter> {

        void listFile(String dir, List<FileExplorer.File> fileList);

        void notifyError(String message);

    }

    interface Presenter extends BasePresenter {
        String getTitle();

        String getDeviceName();

        void listFiles(String path);
    }
}
