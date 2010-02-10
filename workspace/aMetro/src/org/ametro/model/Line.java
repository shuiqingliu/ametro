/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.model;

import android.graphics.Point;
import android.graphics.Rect;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Line implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = -957788093146079549L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(mName);

        out.writeInt(mColor);
        out.writeInt(mLabelColor);
        out.writeInt(mLabelBgColor);


        out.writeInt(mSegments.size());
        for (Segment mSegment : mSegments) {
            out.writeObject(mSegment);
        }

        out.writeInt(mStations.size());
        for (Station station : mStations.values()) {
            out.writeObject(station);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        mName = (String) in.readObject();

        mColor = in.readInt();
        mLabelColor = in.readInt();
        mLabelBgColor = in.readInt();

        mSegments = new ArrayList<Segment>();
        int segmentCount = in.readInt();
        for (int i = 0; i < segmentCount; i++) {
            mSegments.add((Segment) in.readObject());
        }

        mStations = new HashMap<String, Station>();
        int stationCount = in.readInt();
        for (int i = 0; i < stationCount; i++) {
            Station station = (Station) in.readObject();
            mStations.put(station.getName(), station);
        }

        for (Segment segment : mSegments) {
            segment.getFrom().addSegment(segment, Segment.SEGMENT_BEGIN);
            segment.getTo().addSegment(segment, Segment.SEGMENT_END);
        }

    }


    public String mName;
    public int mColor;
    public int mLabelColor;
    public int mLabelBgColor;

    public HashMap<String, Station> mStations = new HashMap<String, Station>();
    public ArrayList<Segment> mSegments = new ArrayList<Segment>();

    public Line(String name, int color, int labelColor, int labelBgColor) {
        super();
        this.mName = name;
        this.mColor = color;
        this.mLabelColor = labelColor;
        this.mLabelBgColor = labelBgColor;
    }

    public String getName() {
        return mName;
    }

    public int getColor() {
        return mColor;
    }

    public int getLabelColor() {
        return mLabelColor;
    }

    public int getLabelBgColor() {
        return mLabelBgColor;
    }

    public Station getStation(String name) {
        return mStations.get(name);
    }

    private Station addStation(String name, Rect r, Point p) {
        Station st = new Station(name, r, p, this);
        mStations.put(name, st);
        return st;
    }

    public Station invalidateStation(String name) {
        Station st = mStations.get(name);
        if (st == null) {
            st = addStation(name, null, null);
        }
        return st;
    }

    public Station invalidateStation(String name, Rect r, Point p) {
        Station st = mStations.get(name);
        if (st == null) {
            st = addStation(name, r, p);
        } else {
            st.setPoint(p);
            st.setRect(r);
        }
        return st;
    }

    public Segment addSegment(Station from, Station to, Double delay) {
        Segment sg = new Segment(from, to, delay);
        mSegments.add(sg);
        Segment opposite = getSegment(to, from);
        if (opposite != null && (opposite.getFlags() & Segment.INVISIBLE) == 0) {
            if (delay == null && opposite.getDelay() != null) {
                sg.addFlag(Segment.INVISIBLE);
            } else if (delay != null && opposite.getDelay() == null) {
                opposite.addFlag(Segment.INVISIBLE);
            } else if (delay == null && opposite.getDelay() == null) {
                sg.addFlag(Segment.INVISIBLE);
            }
        }
        return sg;
    }

    public Collection<Station> getStations() {
        return mStations.values();
    }

    public ArrayList<Segment> getSegments() {
        return mSegments;
    }

    public Segment getSegment(Station from, Station to) {
        final String fromName = from.getName();
        final String toName = to.getName();
        for (Segment seg : mSegments) {
            if (seg.getFrom().getName().equals(fromName) && seg.getTo().getName().equals(toName)) {
                return seg;
            }
        }
        return null;
    }

}
