package org.isisaddons.wicket.gmap3.cpt.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GEvent;
import org.wicketstuff.gmap.api.GEventHandler;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GPoint;
import org.wicketstuff.gmap.api.GPolyline;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;

import org.isisaddons.wicket.gmap3.cpt.applib.Routeable;

public class CollectionOfEntitiesAsRouteables extends PanelAbstract<EntityCollectionModel> {

	private static final long serialVersionUID = 1L;

	private static final String ID_MAP = "map";
    private static final String INVISIBLE_CLASS = "collection-contents-as-routes-invisible";

    private final String apiKey;

    public CollectionOfEntitiesAsRouteables(
            final String id,
            final String apiKey,
            final EntityCollectionModel model) {
        super(id, model);
        this.apiKey = apiKey;
        buildGui();
    }

	private void buildGui() {
		final EntityCollectionModel model = getModel();
        final List<ObjectAdapter> adapterList = model.getObject();

        final GMap map = new GMap(ID_MAP, apiKey);
        map.setStreetViewControlEnabled(true);
        map.setScaleControlEnabled(true);
        map.setScrollWheelZoomEnabled(true);
        map.setPanControlEnabled(true);
        map.setDoubleClickZoomEnabled(true);
 
		for (ObjectAdapter adapter : adapterList) {
			final GLatLng latLng;
			latLng = asGLatLng(((Routeable) adapter).getRoute().get(0));
			if (latLng != null) {
				map.setCenter(latLng);
				break;
			}
		}

		addOrReplace(map);
        addRoute(map, adapterList);
	}

	private void addRoute(final GMap map, final List<ObjectAdapter> adapterList)
    {
    	for (ObjectAdapter adapter : adapterList) {
    		
    		final GPolyline gPolyline = createGPolyline(map, adapter);
    		if(gPolyline != null) {
    			map.addOverlay(gPolyline);
    			addClickListener(gPolyline, adapter);
    		}
    	}	
    }

	private GPolyline createGPolyline(final GMap map, final ObjectAdapter adapter) {
    	// Asumir adapter como lista de Locations
    	final List<GLatLng> puntos = new ArrayList<GLatLng>();
    	final Routeable routeable = (Routeable) adapter;
    	for (final GPoint point : routeable.getRoute())
    		puntos.add(asGLatLng(point));
    	
    	return new GPolyline("RED", 0, 0.5f, (GLatLng[])puntos.toArray());
    }

	private GLatLng asGLatLng(final GPoint point) {
    	return point!=null?new GLatLng(point.getLatitude(), point.getLongitude()):null;
    }

	private ResourceReference determineImageResource(ObjectAdapter adapter) {
        ResourceReference imageResource = null;
        if (adapter != null) {
            imageResource = getImageCache().resourceReferenceFor(adapter);
        }
        return imageResource;
    }

	private void addClickListener(final GPolyline gPolyline, ObjectAdapter adapter) {
        final Class<? extends Page> pageClass = getPageClassRegistry()
                .getPageClass(PageType.ENTITY);
        final PageParameters pageParameters = EntityModel.createPageParameters(
                adapter);

        gPolyline.addListener(GEvent.click, new GEventHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onEvent(AjaxRequestTarget target) {
                setResponsePage(pageClass, pageParameters);
            }
        });
    }

	@Override
    protected void onModelChanged() {
        buildGui();
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        PanelUtil.renderHead(response, CollectionOfEntitiesAsRouteables.class);
    }
    //////////////////////////////////////////////
    // Dependency Injection
    //////////////////////////////////////////////

	@Inject
    private ImageResourceCache imageResourceCache;
    
    @Inject
    private PageClassRegistry pageClassRegistry;

    protected ImageResourceCache getImageCache() {
        return imageResourceCache;
    }

    protected PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry;
    }

}
