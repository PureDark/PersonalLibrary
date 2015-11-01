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

import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.FriendListItem;

public class FriendListDataProvider extends AbstractDataProvider {
    private List<FriendListItem> myFriends;
    private FriendListItem mLastRemovedFriend;
    private int mLastRemovedPosition = -1;

    public FriendListDataProvider(List<FriendListItem> myFriends) {
        this.myFriends = myFriends;
    }

    @Override
    public int getCount() {
        return myFriends.size();
    }

    @Override
    public List<FriendListItem> getItems() {
        return myFriends;
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return myFriends.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedFriend != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < myFriends.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = myFriends.size();
            }

            myFriends.add(insertedPosition, mLastRemovedFriend);

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
        final FriendListItem removedItem = myFriends.remove(position);

        mLastRemovedFriend = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void addItem(Data item) {
        myFriends.add((FriendListItem)item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void addItem(int position, Data item) {
        myFriends.add(position, (FriendListItem)item);
        mLastRemovedPosition = -1;
    }
    @Override
    public void moveItem(int fromPosition, int toPosition) {
        return;
    }
}
