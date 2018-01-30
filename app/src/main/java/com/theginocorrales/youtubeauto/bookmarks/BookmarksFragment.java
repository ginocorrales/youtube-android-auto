package com.theginocorrales.youtubeauto.bookmarks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theginocorrales.youtubeauto.R;
import com.theginocorrales.youtubeauto.utils.GridAutofitLayoutManager;
import com.theginocorrales.youtubeauto.utils.MemoryStorage;

import java.util.ArrayList;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public class BookmarksFragment extends Fragment implements BookmarksClickCallback {
    public static final float BOOKMARK_VIEW_SIZE_IN_DP = 180;
    private BookmarksClickCallback listener;
    private RecyclerView recyclerView;
    private RealmResults<Bookmark> bookmarks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmarks_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBookmarkFragmentClose();
            }
        });
        toolbar.setTitle(R.string.bookmarks);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            listener.onBookmarkFragmentClose();
                            return true;
                    }
                    return false;
                }
                return false;
            }
        });
        Resources r = view.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOOKMARK_VIEW_SIZE_IN_DP, r.getDisplayMetrics());

        recyclerView.setLayoutManager(new GridAutofitLayoutManager(getContext(), (int) px));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Bookmark> bookmarksRealm = realm.where(Bookmark.class).sort("createdAt").findAll();
        final ArrayList<Bookmark> preburntBookmarks = getPreburntBookmarks();
        this.bookmarks = realm.where(Bookmark.class).findAll();
        preburntBookmarks.addAll(bookmarks);
        final BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(preburntBookmarks);
        bookmarksAdapter.setBookmarksClickCallback(this);
        recyclerView.setAdapter(bookmarksAdapter);
        bookmarks.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Bookmark>>() {
            @Override
            public void onChange(@NonNull RealmResults<Bookmark> bookmarks, @javax.annotation.Nullable OrderedCollectionChangeSet changeSet) {
                ArrayList<Bookmark> newBookmarks = new ArrayList<>(preburntBookmarks);
                newBookmarks.addAll(bookmarks);
                bookmarksAdapter.setBookmarks(newBookmarks);
                bookmarksAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.requestFocus();
    }

    private ArrayList<Bookmark> getPreburntBookmarks() {
        final ArrayList<Bookmark> bookmarks = new ArrayList<>();
        bookmarks.add(new Bookmark("YouTube", R.drawable.youtube_favicon, "http://youtube.com", R.drawable.youtube_bookmark));
        String[] storageDirectories = MemoryStorage.getStorageDirectories(getContext());
        String internalSdCard = MemoryStorage.getSdCardPath();
        for (String storageDirectory : storageDirectories) {
            String title;
            if (internalSdCard.startsWith(storageDirectory)) {
                title = getString(R.string.internal_storage);
                bookmarks.add(new Bookmark(title, 0, "file://" + storageDirectory, R.drawable.external_storage));
            } else {
                title = getString(R.string.external_storage);
                bookmarks.add(new Bookmark(title, 0, "file://" + storageDirectory, R.drawable.external_storage));
            }
        }
        bookmarks.add(new Bookmark("Plex.tv", 0, "http://app.plex.tv", R.drawable.plex));
        return bookmarks;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        findParentListener(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        findParentListener(activity);
    }

    private void findParentListener(Context context) {
        if (getParentFragment() != null && getParentFragment() instanceof BookmarksClickCallback) {
            listener = (BookmarksClickCallback) getParentFragment();
        } else if (context instanceof BookmarksClickCallback) {
            listener = (BookmarksClickCallback) context;
        } else {
            throw new IllegalStateException(context + " should implement " + BookmarksClickCallback.class.getSimpleName());
        }
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        listener.onBookmarkSelected(bookmark);
        listener.onBookmarkFragmentClose();
    }

    @Override
    public void onBookmarkAddCurrentPage() {
        listener.onBookmarkAddCurrentPage();
    }

    @Override
    public void onBookmarkDelete(Bookmark bookmark) {
        listener.onBookmarkDelete(bookmark);
    }

    @Override
    public void onBookmarkFragmentClose() {
        listener.onBookmarkFragmentClose();
    }
}
