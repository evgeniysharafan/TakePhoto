package com.evgeniysharafan.takephoto.util.lib;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.DataSetObserver;

import java.security.InvalidParameterException;
import java.text.Collator;

/**
 * A variant of MergeCursor that sorts the cursors being merged.
 */
public class SortCursor extends AbstractCursor {

    private Cursor cursor; // updated in onMove
    private Cursor[] cursors;
    private int[] sortColumns;

    private final int rowCacheSize = 64;
    private int rowNumCache[] = new int[rowCacheSize];
    private int cursorCache[] = new int[rowCacheSize];

    private int curRowNumCache[][];
    private int lastCacheHit = -1;
    private Class columnType = String.class;
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            // Reset our position so the optimizations in move-related code
            // don't screw us over
            L.d("Data set changes");
            mPos = -1;
        }

        @Override
        public void onInvalidated() {
            mPos = -1;
        }
    };

    public SortCursor(Cursor[] cursors, String sortColumn) {
        init(cursors, sortColumn);
    }

    public SortCursor(Cursor[] cursors, String sortColumn, Class columnType) {
        this.columnType = columnType;
        init(cursors, sortColumn);
    }

    private void init(Cursor[] cursors, String sortColumn) {
        this.cursors = cursors;
        int length = this.cursors.length;
        sortColumns = new int[length];
        for (int i = 0; i < length; i++) {
            if (this.cursors[i] == null) continue;

            // Register ourself as a data set observer
            this.cursors[i].registerDataSetObserver(observer);
            this.cursors[i].moveToFirst();

            // We don't catch the exception
            sortColumns[i] = this.cursors[i].getColumnIndexOrThrow(sortColumn);
        }

        cursor = null;
        for (int i = rowNumCache.length - 1; i >= 0; i--) {
            rowNumCache[i] = -2;
        }

        curRowNumCache = new int[rowCacheSize][length];
    }

    private int compare(int oldPosition, int newPosition, int length) {
        int result;
        if (columnType == String.class) {
            result = compareStrings(oldPosition, newPosition, length);
        } else if (columnType == Long.class) {
            result = compareLong(oldPosition, newPosition, length);
        } else {
            throw new InvalidParameterException("Could not process sorting with type " + columnType);
        }

        return result;
    }

    private int compareLong(int oldPosition, int newPosition, int length) {
        // search forward to the new position
        int biggestIndex = -1;
        for (int i = oldPosition; i <= newPosition; i++) {
            long biggest = 0L;
            biggestIndex = -1;
            for (int j = 0; j < length; j++) {
                if (cursors[j] == null || cursors[j].isAfterLast()) {
                    continue;
                }

                long current = cursors[j].getLong(sortColumns[j]);
                if (biggestIndex < 0 || current > biggest) {
                    biggest = current;
                    biggestIndex = j;
                }

            }
            if (i == newPosition) break;
            if (cursors[biggestIndex] != null) {
                cursors[biggestIndex].moveToNext();
            }
        }

        return biggestIndex;
    }

    private int compareStrings(int oldPosition, int newPosition, int length) {
        // search forward to the new position
        int smallestIdx = -1;
        Collator collator = Collator.getInstance();
        for (int i = oldPosition; i <= newPosition; i++) {
            String smallest = "";
            smallestIdx = -1;
            for (int j = 0; j < length; j++) {
                if (cursors[j] == null || cursors[j].isAfterLast()) {
                    continue;
                }

                String current = cursors[j].getString(sortColumns[j]);
                if (smallestIdx < 0 || collator.compare(current, smallest) < 0) {
                    smallest = current;
                    smallestIdx = j;
                }

            }
            if (i == newPosition) break;
            if (cursors[smallestIdx] != null) {
                cursors[smallestIdx].moveToNext();
            }
        }

        return smallestIdx;
    }

    @Override
    public int getCount() {
        int count = 0;
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] != null) {
                count += cursors[i].getCount();
            }
        }

        return count;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }

        /* Find the right cursor
         * Because the client of this cursor (the listadapter/view) tends
         * to jump around in the cursor somewhat, a simple cache strategy
         * is used to avoid having to search all cursors from the start.
         * TODO: investigate strategies for optimizing random access and
         * reverse-order access.
         */
        int cache_entry = newPosition % rowCacheSize;
        if (rowNumCache[cache_entry] == newPosition) {
            int which = cursorCache[cache_entry];
            cursor = cursors[which];
            if (cursor == null) {
                L.w("onMove: cache results in a null cursor.");
                return false;
            }

            cursor.moveToPosition(curRowNumCache[cache_entry][which]);
            lastCacheHit = cache_entry;

            return true;
        }

        cursor = null;
        int length = cursors.length;
        if (lastCacheHit >= 0) {
            for (int i = 0; i < length; i++) {
                if (cursors[i] == null) continue;
                cursors[i].moveToPosition(curRowNumCache[lastCacheHit][i]);
            }
        }

        if (newPosition < oldPosition || oldPosition == -1) {
            for (int i = 0; i < length; i++) {
                if (cursors[i] == null) continue;
                cursors[i].moveToFirst();
            }

            oldPosition = 0;
        }

        if (oldPosition < 0) {
            oldPosition = 0;
        }

        int selectedIndex = compare(oldPosition, newPosition, length);
        cursor = cursors[selectedIndex];

        rowNumCache[cache_entry] = newPosition;
        cursorCache[cache_entry] = selectedIndex;

        for (int i = 0; i < length; i++) {
            if (cursors[i] != null) {
                curRowNumCache[cache_entry][i] = cursors[i].getPosition();
            }
        }

        lastCacheHit = -1;

        return true;
    }

    @Override
    public String getString(int column) {
        return cursor.getString(column);
    }

    @Override
    public short getShort(int column) {
        return cursor.getShort(column);
    }

    @Override
    public int getInt(int column) {
        return cursor.getInt(column);
    }

    @Override
    public long getLong(int column) {
        return cursor.getLong(column);
    }

    @Override
    public float getFloat(int column) {
        return cursor.getFloat(column);
    }

    @Override
    public double getDouble(int column) {
        return cursor.getDouble(column);
    }

    @Override
    public int getType(int column) {
        return cursor.getType(column);
    }

    @Override
    public boolean isNull(int column) {
        return cursor.isNull(column);
    }

    @Override
    public byte[] getBlob(int column) {
        return cursor.getBlob(column);
    }

    @Override
    public String[] getColumnNames() {
        if (cursor != null) {
            return cursor.getColumnNames();
        } else {
            // All of the cursors may be empty, but they can still return
            // this information.
            int length = cursors.length;
            for (int i = 0; i < length; i++) {
                if (cursors[i] != null) {
                    return cursors[i].getColumnNames();
                }
            }

            throw new IllegalStateException("No cursor that can return names");
        }
    }

    @Override
    public void deactivate() {
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] == null) continue;
            cursors[i].deactivate();
        }
    }

    @Override
    public void close() {
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] == null) continue;
            cursors[i].close();
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        L.d("registerDataSetObserver");
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] != null) {
                cursors[i].registerDataSetObserver(observer);
            }
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        L.d("unregisterDataSetObserver");
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] != null) {
                cursors[i].unregisterDataSetObserver(observer);
            }
        }
    }

    @Override
    public boolean requery() {
        int length = cursors.length;
        for (int i = 0; i < length; i++) {
            if (cursors[i] == null) continue;

            if (!cursors[i].requery()) {
                return false;
            }
        }

        return true;
    }
}
