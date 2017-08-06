package com.kami.fileexplorer.data.cifs;

import com.kami.fileexplorer.bean.CIFSDevice;
import com.kami.fileexplorer.data.FileExplorer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * author: youyi_sizuru
 * data: 2017/7/23
 */

public class CIFSFileExplorer implements FileExplorer {
    private CIFSDevice mDevice;

    public CIFSFileExplorer(CIFSDevice device) {
        mDevice = device;
    }

    @Override
    public String getTitle() {
        return "网络邻居";
    }

    @Override
    public List<File> getFiles(String path) throws IOException {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", "youyi", "xzp");
        SmbFile parent = new SmbFile(String.format("smb://%s/%s/", mDevice.getHostIp(), path), auth);
        if (!parent.exists()) {
            throw new IOException(String.format("%s is not exists", path));
        }
        SmbFile[] children = parent.listFiles();
        List<File> fileList = new ArrayList<>();
        for (SmbFile child : children) {
            CIFSFile file = new CIFSFile();
            String name = child.getName();
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            file.setName(name);
            boolean isDir = child.isDirectory();
            file.setDir(isDir);
            if (!isDir) {
                file.setLastModified(child.getLastModified());
                file.setLength(child.getContentLength());
            }
            fileList.add(file);
        }
        return fileList;
    }

    @Override
    public String getDeviceName() {
        return mDevice.getHostName();
    }

    public static class CIFSFile implements File {
        private String name;
        private boolean isDir;
        private int length;
        private long lastModified;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isDirectory() {
            return isDir;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public long lastModified() {
            return lastModified;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDir(boolean dir) {
            isDir = dir;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
    }
}
