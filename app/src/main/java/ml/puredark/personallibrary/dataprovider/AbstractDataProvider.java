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

public abstract class AbstractDataProvider {

    public static abstract class Data {
        public abstract long getId();

        public abstract void setPinned(boolean pinned);

        public abstract boolean isPinned();
    }

    public abstract int getCount();

    public abstract List<BookListItem> getItems();

    public abstract Data getItem(int index);

    public abstract void removeItem(int position);

    public abstract void addItem(Data item);
    public abstract void addItem(int position, Data item);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract int undoLastRemoval();
}
