/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.wicket.gmap3.fixture.dom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.wicketstuff.gmap.api.GPoint;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;

import org.isisaddons.wicket.gmap3.cpt.applib.Locatable;
import org.isisaddons.wicket.gmap3.cpt.applib.Location;
import org.isisaddons.wicket.gmap3.cpt.applib.Routeable;
import org.isisaddons.wicket.gmap3.cpt.service.LocationLookupService;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
         column="id")
@javax.jdo.annotations.Version(
        strategy=VersionStrategy.VERSION_NUMBER, 
        column="version")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name="ToDoItem_description_must_be_unique", 
            members={"ownedBy","description"})
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "todo_all",
            value = "SELECT "
                    + "FROM org.isisaddons.wicket.gmap3.fixture.dom.Gmap3ToDoItem "
                    + "WHERE ownedBy == :ownedBy"),
    @javax.jdo.annotations.Query(
            name = "todo_notYetComplete",
            value = "SELECT "
                    + "FROM org.isisaddons.wicket.gmap3.fixture.dom.Gmap3ToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "   && complete == false"),
    @javax.jdo.annotations.Query(
            name = "todo_complete",
            value = "SELECT "
                    + "FROM org.isisaddons.wicket.gmap3.fixture.dom.Gmap3ToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "&& complete == true"),
    @javax.jdo.annotations.Query(
            name = "todo_autoComplete",
            value = "SELECT "
                    + "FROM org.isisaddons.wicket.gmap3.fixture.dom.Gmap3ToDoItem "
                    + "WHERE ownedBy == :ownedBy && "
                    + "description.indexOf(:description) >= 0")
})
@DomainObject(
        objectType = "TODO",
        autoCompleteRepository = Gmap3WicketToDoItems.class
)
@DomainObjectLayout(
        named = "ToDo Item",
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class Gmap3ToDoItem implements Comparable<Gmap3ToDoItem>, Locatable, Routeable {

    //region > identification in the UI

    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getDescription());
        if (isComplete()) {
            buf.append("- Completed!");
        }
        return buf.toString();
    }
    
    public String iconName() {
        return "ToDoItem-" + (!isComplete() ? "todo" : "done");
    }

    //endregion

    //region > description (property)

    private String description;

    @javax.jdo.annotations.Column(allowsNull="false", length=100)
    @MemberOrder(sequence="1")
    @Property(
            regexPattern = "\\w[@&:\\-\\,\\.\\+ \\w]*"
    )
    @PropertyLayout(
            typicalLength = 50
    )
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    //endregion

    //region > ownedBy (property)

    private String ownedBy;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            hidden = Where.EVERYWHERE
    )
    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(final String ownedBy) {
        this.ownedBy = ownedBy;
    }

    //endregion

    //region > complete (property), completed (action), notYetCompleted (action)
    
    private boolean complete;

    @Property(
            editing = Editing.DISABLED
    )
    @MemberOrder(sequence="2")
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    @MemberOrder(name="complete", sequence="1")
    public Gmap3ToDoItem completed() {
        setComplete(true);
        return this;
    }
    // disable action dependent on state of object
    public String disableCompleted() {
        return isComplete() ? "Already completed" : null;
    }

    @MemberOrder(name="complete", sequence="2")
    public Gmap3ToDoItem notYetCompleted() {
        setComplete(false);

        return this;
    }
    // disable action dependent on state of object
    public String disableNotYetCompleted() {
        return !complete ? "Not yet completed" : null;
    }

    //endregion

    //region > location

    private Double locationLatitude;
    private Double locationLongitude;

    @Property(
            optionality = Optionality.OPTIONAL,
            editing = Editing.DISABLED
    )
    @MemberOrder(sequence="3")
    public Location getLocation() {
        return locationLatitude != null && locationLongitude != null? new Location(locationLatitude, locationLongitude): null;
    }
    public void setLocation(final Location location) {
        locationLongitude = location != null ? location.getLongitude() : null;
        locationLatitude = location != null ? location.getLatitude() : null;
    }

    @MemberOrder(name="location", sequence="1")
    public Gmap3ToDoItem updateLocation(
            @ParameterLayout(named="Address") final String address) {
        final Location location = this.locationLookupService.lookup(address);
        setLocation(location);
        return this;
    }

    //endregion

    //region > dependencies (collection), add (action), remove (action)

    // overrides the natural ordering
    public static class DependenciesComparator implements Comparator<Gmap3ToDoItem> {
        @Override
        public int compare(final Gmap3ToDoItem p, final Gmap3ToDoItem q) {
            final Ordering<Gmap3ToDoItem> byDescription = new Ordering<Gmap3ToDoItem>() {
                public int compare(final Gmap3ToDoItem p, final Gmap3ToDoItem q) {
                    return Ordering.natural().nullsFirst().compare(p.getDescription(), q.getDescription());
                }
            };
            return byDescription
                    .compound(Ordering.<Gmap3ToDoItem>natural())
                    .compare(p, q);
        }
    }



    @javax.jdo.annotations.Persistent(table="Gmap3ToDoItemDependencies")
    @javax.jdo.annotations.Join(column="dependingId")
    @javax.jdo.annotations.Element(column="dependentId")
    private SortedSet<Gmap3ToDoItem> dependencies = new TreeSet<>();

    @CollectionLayout(
            sortedBy = DependenciesComparator.class,
            render = RenderType.EAGERLY
    )
    public SortedSet<Gmap3ToDoItem> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final SortedSet<Gmap3ToDoItem> dependencies) {
        this.dependencies = dependencies;
    }

    
    @MemberOrder(name="dependencies", sequence="1")
    public Gmap3ToDoItem add(final Gmap3ToDoItem toDoItem) {
        getDependencies().add(toDoItem);
        return this;
    }
    public List<Gmap3ToDoItem> autoComplete0Add(final @MinLength(2) String search) {
        final List<Gmap3ToDoItem> list = toDoItems.autoComplete(search);
        list.removeAll(getDependencies());
        list.remove(this);
        return list;
    }

    public String disableAdd(final Gmap3ToDoItem toDoItem) {
        if(isComplete()) {
            return "Cannot add dependencies for items that are complete";
        }
        return null;
    }
    // validate the provided argument prior to invoking action
    public String validateAdd(final Gmap3ToDoItem toDoItem) {
        if(getDependencies().contains(toDoItem)) {
            return "Already a dependency";
        }
        if(toDoItem == this) {
            return "Can't set up a dependency to self";
        }
        return null;
    }

    @MemberOrder(name="dependencies", sequence="2")
    public Gmap3ToDoItem remove(final Gmap3ToDoItem toDoItem) {
        getDependencies().remove(toDoItem);
        return this;
    }
    // disable action dependent on state of object
    public String disableRemove(final Gmap3ToDoItem toDoItem) {
        if(isComplete()) {
            return "Cannot remove dependencies for items that are complete";
        }
        return getDependencies().isEmpty()? "No dependencies to remove": null;
    }
    // validate the provided argument prior to invoking action
    public String validateRemove(final Gmap3ToDoItem toDoItem) {
        if(!getDependencies().contains(toDoItem)) {
            return "Not a dependency";
        }
        return null;
    }
    // provide a drop-down
    public Collection<Gmap3ToDoItem> choices0Remove() {
        return getDependencies();
    }

    //endregion

    //region > predicates

    public static class Predicates {
        
        public static Predicate<Gmap3ToDoItem> thoseOwnedBy(final String currentUser) {
            return toDoItem -> Objects.equal(toDoItem.getOwnedBy(), currentUser);
        }

        public static Predicate<Gmap3ToDoItem> thoseCompleted(
                final boolean completed) {
            return t -> Objects.equal(t.isComplete(), completed);
        }

        public static Predicate<Gmap3ToDoItem> thoseWithSimilarDescription(final String description) {
            return t -> t.getDescription().contains(description);
        }

        public static Predicate<Gmap3ToDoItem> thoseNot(final Gmap3ToDoItem toDoItem) {
            return t -> t != toDoItem;
        }

    }

    //endregion

    //region > Routeable

    @NotPersistent
    private List<String> points = new ArrayList<>();

    @Override
    public List<GPoint> getRoute() {
        final List<GPoint> route = new ArrayList<GPoint>();
        for (String point : points) {
            String[] s = point.split(";");
            route.add(s[0] != null && s[1] != null ? new GPoint(Float
                    .valueOf(s[1]), Float.valueOf(s[0])) : null);
        }
        return route;
    }

    @Programmatic
    public void loadPointsFrom(final URL resource) {
        InputStream inputStream = null;
        final ByteSource byteSource = Resources.asByteSource(resource);

        List sheetData = new ArrayList();
        try {
            inputStream = byteSource.openStream();
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();

            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                Iterator cells = row.cellIterator();
                List data = new ArrayList();

                while (cells.hasNext()) {
                    HSSFCell cell = (HSSFCell) cells.next();
                    data.add(cell);
                }

                sheetData.add(data);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        copyToPoints(sheetData, points);
    }

    private void copyToPoints(final List sheetData, final List<String> points) {

        for (int i = 4; i < sheetData.size(); i++) {

            final List list = (List) sheetData.get(i);

            String point = "";
            for (int j = 2; j < 4; j++) {

                final Cell cell = (Cell) list.get(j);

                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    point = point + String.valueOf(cell.getNumericCellValue());
                    point = point + ";";
                } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    point = point + cell.getRichStringCellValue();
                    point = point + ";";
                }
            }
            points.add(point);
        }
    }

    //endregion

    //region > toString, compareTo

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "description,complete,ownedBy");
    }
        
    @Override
    public int compareTo(final Gmap3ToDoItem other) {
        return ObjectContracts.compare(this, other, "complete,description");
    }

    //endregion

    //region > injected services

    @javax.inject.Inject
    @SuppressWarnings("unused")
    private DomainObjectContainer container;

    @javax.inject.Inject
    private Gmap3WicketToDoItems toDoItems;

    @javax.inject.Inject
    @SuppressWarnings("unused")
    private ClockService clockService;

    @javax.inject.Inject
    private LocationLookupService locationLookupService;


    //endregion

}
