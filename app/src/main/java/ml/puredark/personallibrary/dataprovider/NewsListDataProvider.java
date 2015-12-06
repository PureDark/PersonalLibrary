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

import java.util.List;

import ml.puredark.personallibrary.beans.NewsListItem;

public class NewsListDataProvider extends AbstractDataProvider {
    private List<NewsListItem> myNews;
    private NewsListItem mLastRemovedFriend;
    private int mLastRemovedPosition = -1;

    public NewsListDataProvider(List<NewsListItem> myNews) {
        this.myNews = myNews;
    }

    @Override
    public int getCount() {
        return myNews.size();
    }

    @Override
    public List<NewsListItem> getItems() {
        return myNews;
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return myNews.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedFriend != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < myNews.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = myNews.size();
            }

            myNews.add(insertedPosition, mLastRemovedFriend);

            mLastRemovedFriend = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }



    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final NewsListItem removedItem = myNews.remove(position);

        mLastRemovedFriend = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void addItem(Data item) {
        myNews.add((NewsListItem)item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void addItem(int position, Data item) {
        myNews.add(position, (NewsListItem)item);
        mLastRemovedPosition = -1;
    }
    @Override
    public void moveItem(int fromPosition, int toPosition) {
        return;
    }
}
