package com.kami.fileexplorer.ui.file;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kami.fileexplorer.R;
import com.kami.fileexplorer.bean.FileRoute;
import com.kami.fileexplorer.comparable.DefaultFileComparator;
import com.kami.fileexplorer.data.FileExplorer;
import com.kami.fileexplorer.ui.BaseAdapter;
import com.kami.fileexplorer.ui.BaseFragment;
import com.kami.fileexplorer.widget.FSRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import jcifs.smb.SmbAuthException;

/**
 * author: youyi_sizuru
 * data: 2017/7/23
 */

public class FileFragment extends BaseFragment implements FileContract.View, BaseAdapter.OnItemClickListener {
    private FileContract.Presenter mPresenter;
    @BindView(R.id.file_list)
    FSRecyclerView mFileListView;
    private FileAdapter mFileAdapter;
    @BindView(R.id.file_route_list)
    FSRecyclerView mFileRouteListView;
    private FileRouteAdapter mFileRouteAdapter;
    private Stack<List<FileExplorer.File>> mFileListStack;
    private Comparator<FileExplorer.File> mFileComparator;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_file, container, false);
    }

    @Override
    public void setPresenter(FileContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFileListStack = new Stack<>();
        mFileComparator = new DefaultFileComparator();
        initFileListView();
        initFileRouteListView();
        mPresenter.subscribe();
    }

    @Override
    public void onItemClick(BaseAdapter adapter, int position, View view) {
        if (adapter == mFileAdapter) {
            FileExplorer.File file = mFileAdapter.getList().get(position);
            if (file.isDirectory()) {
                String path = getPath() + "/" + file.getName();
                mFileRouteAdapter.add(new FileRoute(file.getName(), path));
                mFileRouteListView.scrollToPosition(mFileRouteAdapter.getItemCount() - 1);
                mFileListStack.add(new ArrayList<>());
                mPresenter.listFiles();
            }
        } else {
            if (!backTo(position)) {
                getActivity().finish();
            }
        }
    }

    boolean backTo() {
        int position = mFileRouteAdapter.getItemCount() - 2;
        return backTo(position);
    }

    boolean backTo(int position) {
        if (position < 1) {
            return false;
        }
        int removeSize = mFileRouteAdapter.getItemCount() - 1 - position;
        mFileRouteAdapter.removeTo(position);
        for (int i = 0; i < removeSize; i++) {
            mFileListStack.pop();
        }
        mFileAdapter.setList(mFileListStack.peek());
        return true;
    }

    @Override
    public String getPath() {
        List<FileRoute> fileRouteList = mFileRouteAdapter.getList();
        int size = mFileRouteAdapter.getItemCount();
        return fileRouteList.get(size - 1).getPath();
    }

    private void initFileListView() {
        mFileListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mFileAdapter = new FileAdapter(getContext());
        mFileAdapter.setItemClickListener(this);
        mFileListView.setAdapter(mFileAdapter);
    }

    private void initFileRouteListView() {
        mFileRouteListView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<FileRoute> list = new ArrayList<>();
        list.add(new FileRoute(mPresenter.getTitle(), mPresenter.getTitle()));
        list.add(new FileRoute(mPresenter.getDeviceName(), "/"));
        mFileRouteAdapter = new FileRouteAdapter(this.getContext(), list);
        mFileListStack.push(new ArrayList<>());
        mFileRouteAdapter.setItemClickListener(this);
        mFileRouteListView.setAdapter(mFileRouteAdapter);
    }

    @Override
    public void listFile(List<FileExplorer.File> fileList) {
        Collections.sort(fileList, mFileComparator);
        mFileListStack.pop();
        mFileListStack.push(fileList);
        mFileAdapter.setList(fileList);
    }


    @Override
    public void notifyError(Throwable throwable) {
        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        mPresenter.unSubscribe();
        super.onDestroyView();
    }
}