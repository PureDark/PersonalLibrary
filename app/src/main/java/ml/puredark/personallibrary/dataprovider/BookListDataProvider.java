/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ml.puredark.personallibrary.dataprovider;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.LinkedList;
import java.util.List;

import ml.puredark.personallibrary.beans.BookListItem;

public class BookListDataProvider extends AbstractDataProvider {
    private List<BookListItem> myBooks;
    private BookListItem mLastRemovedBook;
    private int mLastRemovedPosition = -1;

    public BookListDataProvider(List<BookListItem> myBooks) {
        this.myBooks = myBooks;
    }

    @Override
    public int getCount() {
        return myBooks.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return myBooks.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedBook != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < myBooks.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = myBooks.size();
            }

            myBooks.add(insertedPosition, mLastRemovedBook);

            mLastRemovedBook = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final BookListItem item = myBooks.remove(fromPosition);

        myBooks.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final BookListItem removedItem = myBooks.remove(position);

        mLastRemovedBook = removedItem;
        mLastRemovedPosition = position;
    }
}
