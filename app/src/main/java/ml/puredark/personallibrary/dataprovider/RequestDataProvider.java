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

import ml.puredark.personallibrary.beans.BorrowRecord;
import ml.puredark.personallibrary.beans.Request;

public class RequestDataProvider extends AbstractDataProvider {
    private List<Request> myRequests;

    public RequestDataProvider(List<Request> myRequests) {
        this.myRequests = myRequests;
    }

    @Override
    public int getCount() {
        return myRequests.size();
    }

    @Override
    public List<Request> getItems() {
        return myRequests;
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return myRequests.get(index);
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
        myRequests.add((Request)item);
    }

    @Override
    public void addItem(int position, Data item) {
        myRequests.add(position, (Request)item);
    }
    @Override
    public void moveItem(int fromPosition, int toPosition) {
        return;
    }
}
