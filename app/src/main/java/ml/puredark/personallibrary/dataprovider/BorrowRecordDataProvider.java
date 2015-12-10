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

import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.beans.BorrowRecord;

public class BorrowRecordDataProvider extends AbstractDataProvider {
    private List<BorrowRecord> myBorrowRecords;

    public BorrowRecordDataProvider(List<BorrowRecord> myBookMarks) {
        this.myBorrowRecords = myBookMarks;
    }

    @Override
    public int getCount() {
        return myBorrowRecords.size();
    }

    @Override
    public List<BorrowRecord> getItems() {
        return myBorrowRecords;
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return myBorrowRecords.get(index);
    }

    @Override
    public int undoLastRemoval() {
            return -1;
    }



    @Override
    public void removeItem(int position) {
    }

    @Override
    public void addItem(Data item) {
        myBorrowRecords.add((BorrowRecord)item);
    }

    @Override
    public void addItem(int position, Data item) {
        myBorrowRecords.add(position, (BorrowRecord)item);
    }
    @Override
    public void moveItem(int fromPosition, int toPosition) {
        return;
    }
}
