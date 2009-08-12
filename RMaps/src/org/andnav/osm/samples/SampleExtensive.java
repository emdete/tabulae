package org.andnav.osm.samples;

import org.andnav.osm.OpenStreetMapActivity;
import com.robert.maps.R;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.OpenStreetMapViewSimpleLocationOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class SampleExtensive extends OpenStreetMapActivity implements OpenStreetMapConstants{
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
	private static final int MENU_RENDERER_ID = MENU_ZOOMOUT_ID + 1;
	private static final int MENU_ANIMATION_ID = MENU_RENDERER_ID + 1;
	private static final int MENU_MINIMAP_ID = MENU_ANIMATION_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv, mOsmvMinimap; 
	private OpenStreetMapViewSimpleLocationOverlay mMyLocationOverlay; 

	// ===========================================================
	// Constructors
	// ===========================================================
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false); // Pass true here to actually contribute to OSM!
        
        final RelativeLayout rl = new RelativeLayout(this);
        
        this.mOsmv = new OpenStreetMapView(this, OpenStreetMapRendererInfo.MAPNIK);
        rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        /* SingleLocation-Overlay */
        {
	        /* Create a static Overlay showing a single location. (Gets updated in onLocationChanged(Location loc)! */
	        this.mMyLocationOverlay = new OpenStreetMapViewSimpleLocationOverlay(this);
	        this.mOsmv.getOverlays().add(mMyLocationOverlay);
        }
        
        /* ZoomControls */
        {
	        /* Create a ImageView with a zoomIn-Icon. */
	        final ImageView ivZoomIn = new ImageView(this);
	        ivZoomIn.setImageResource(R.drawable.zoom_in);
	        /* Create RelativeLayoutParams, that position in in the top right corner. */
	        final RelativeLayout.LayoutParams zoominParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        rl.addView(ivZoomIn, zoominParams);
	        
	        ivZoomIn.setOnClickListener(new OnClickListener(){
				// @Override
				public void onClick(View v) {
					SampleExtensive.this.mOsmv.zoomIn();
				}
	        });
	        
	        
	        /* Create a ImageView with a zoomOut-Icon. */
	        final ImageView ivZoomOut = new ImageView(this);
	        ivZoomOut.setImageResource(R.drawable.zoom_out);
	        
	        /* Create RelativeLayoutParams, that position in in the top left corner. */
	        final RelativeLayout.LayoutParams zoomoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        rl.addView(ivZoomOut, zoomoutParams);
	        
	        ivZoomOut.setOnClickListener(new OnClickListener(){
				// @Override
				public void onClick(View v) {
					SampleExtensive.this.mOsmv.zoomOut();
				}
	        });
        }
        
        /* MiniMap */
        {
	        /* Create another OpenStreetMapView, that will act as the MiniMap for the 'MainMap'. They will share the TileProvider. */
	        mOsmvMinimap = new OpenStreetMapView(this, OpenStreetMapRendererInfo.CLOUDMADESTANDARDTILES, this.mOsmv);
	        final int aZoomDiff = 3; // Use OpenStreetMapViewConstants.NOT_SET to disable autozooming of this minimap
	        this.mOsmv.setMiniMap(mOsmvMinimap, aZoomDiff);
	        
	        
	        /* Create RelativeLayout.LayoutParams that position the MiniMap on the top-right corner of the RelativeLayout. */
	        RelativeLayout.LayoutParams minimapParams = new RelativeLayout.LayoutParams(90, 90);
	        minimapParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        minimapParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	        minimapParams.setMargins(5,5,5,5);
	        rl.addView(mOsmvMinimap, minimapParams);
        }
        
        this.setContentView(rl);
    }

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
    
	@Override
	public void onLocationChanged(final Location pLoc) {
		this.mMyLocationOverlay.setLocation(TypeConverter.locationToGeoPoint(pLoc));
	}

	@Override
	public void onLocationLost() {
		// We'll do nothing here. 
	}
	
    
    @Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
    	pMenu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
    	pMenu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");
    	
    	final SubMenu subMenu = pMenu.addSubMenu(0, MENU_RENDERER_ID, Menu.NONE, "Choose Renderer");
    	{
	    	for(int i = 0; i < OpenStreetMapRendererInfo.values().length; i ++)
	    		subMenu.add(0, 1000 + i, Menu.NONE, OpenStreetMapRendererInfo.values()[i].NAME);
    	}
    	
    	pMenu.add(0, MENU_ANIMATION_ID, Menu.NONE, "Run Animation");
    	pMenu.add(0, MENU_MINIMAP_ID, Menu.NONE, "Toggle Minimap");
    	
    	return true;
	}
    
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
			case MENU_ZOOMIN_ID:
				this.mOsmv.zoomIn();
				return true;
				
			case MENU_ZOOMOUT_ID:
				this.mOsmv.zoomOut();
				return true;
				
			case MENU_RENDERER_ID:
				this.mOsmv.invalidate();
				return true;
				
			case MENU_MINIMAP_ID:
				switch(this.mOsmv.getOverrideMiniMapVisiblity()){
					case View.VISIBLE:
						this.mOsmv.setOverrideMiniMapVisiblity(View.INVISIBLE);
						break;
					case NOT_SET:
					case View.INVISIBLE:
					case View.GONE:
						this.mOsmv.setOverrideMiniMapVisiblity(View.VISIBLE);
						break;
				}					
				return true;
				
			case MENU_ANIMATION_ID:
				this.mOsmv.getController().animateTo(52370816, 9735936, OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT); // Hannover
				// Stop the Animation after 500ms  (just to show that it works)
//				new Handler().postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						SampleExtensive.this.mOsmv.getController().stopAnimation(false);
//					}
//				}, 500);
				return true;
				
			default: 
				this.mOsmv.setRenderer(OpenStreetMapRendererInfo.values()[item.getItemId() - 1000]);
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}