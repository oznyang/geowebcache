package org.geowebcache.service.arcgis.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.arcgis.config.CacheInfo;
import org.geowebcache.arcgis.config.LODInfo;
import org.geowebcache.arcgis.layer.ArcGISCacheLayer;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.Grid;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:oxsean@gmail.com">sean yang</a>
 * @version V1.0, 12-6-20
 */
public final class InfoGenerator {

    private static final Log LOG = LogFactory.getLog(InfoGenerator.class);
    private static Template jsapiTpl;

    static {
        try {
            jsapiTpl = new Template(null, new InputStreamReader(InfoGenerator.class.getResource("jsapi.ftl").openStream()), new Configuration());
        } catch (IOException ignored) {
        }
    }

    public InfoGenerator(HttpServletRequest request, TileLayer layer) {
        this.request = request;
        this.layer = layer;
    }

    private HttpServletRequest request;
    private TileLayer layer;

    public String generateJson() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("mapName", "Layers");
        map.put("units", "esriMeters");
        map.put("supportedImageFormatTypes", "PNG24,PNG,JPG,DIB,TIFF,EMF,PS,PDF,GIF,SVG,SVGZ,AI,BMP");
        CacheInfo cacheInfo = null;
        int wkid = 2364;
        GridSubset gridSubset = layer.getGridSubset(layer.getGridSubsets().iterator().next());
        if (layer instanceof ArcGISCacheLayer) {
            cacheInfo = ((ArcGISCacheLayer) layer).getCacheInfo();
            wkid = cacheInfo.getTileCacheInfo().getSpatialReference().getWKID();
        } else {
            try {
                wkid = Integer.parseInt(gridSubset.getName().substring(5, 9));
            } catch (NumberFormatException ignored) {
            }
        }
        Map<String, Object> extent = new LinkedHashMap<String, Object>();
        BoundingBox boundingBox = gridSubset.getOriginalExtent();
        extent.put("xmin", boundingBox.getMinX());
        extent.put("ymin", boundingBox.getMinY());
        extent.put("xmax", boundingBox.getMaxX());
        extent.put("ymax", boundingBox.getMaxY());
        extent.put("spatialReference", Collections.singletonMap("wkid", wkid));
        map.put("initialExtent", extent);
        map.put("fullExtent", extent);
        map.put("singleFusedMapCache", true);
        map.put("spatialReference", Collections.singletonMap("wkid", wkid));
        map.put("layers", Collections.emptyList());
        if (false) {
            Map<String, Object> tileInfo = new LinkedHashMap<String, Object>();
            tileInfo.put("rows", cacheInfo.getTileCacheInfo().getTileRows());
            tileInfo.put("cols", cacheInfo.getTileCacheInfo().getTileCols());
            tileInfo.put("dpi", cacheInfo.getTileCacheInfo().getDPI());
            tileInfo.put("format", cacheInfo.getTileImageInfo().getCacheTileFormat());
            tileInfo.put("compressionQuality", cacheInfo.getTileImageInfo().getCompressionQuality());
            Map<String, Double> origin = new HashMap<String, Double>();
            origin.put("x", cacheInfo.getTileCacheInfo().getTileOrigin().getX());
            origin.put("y", cacheInfo.getTileCacheInfo().getTileOrigin().getY());
            tileInfo.put("origin", origin);
            tileInfo.put("spatialReference", Collections.singletonMap("wkid", wkid));
            List<Map<String, Number>> lods = new ArrayList<Map<String, Number>>();
            for (LODInfo lod : cacheInfo.getTileCacheInfo().getLodInfos()) {
                Map<String, Number> l = new HashMap<String, Number>();
                l.put("level", lod.getLevelID());
                l.put("resolution", lod.getResolution());
                l.put("scale", lod.getScale());
                lods.add(l);
            }
            tileInfo.put("lods", lods);
            map.put("tileInfo", tileInfo);
        } else {
            Map<String, Object> tileInfo = new LinkedHashMap<String, Object>();
            tileInfo.put("rows", gridSubset.getTileHeight());
            tileInfo.put("cols", gridSubset.getTileWidth());
            tileInfo.put("dpi", (int) gridSubset.getDotsPerInch());
            tileInfo.put("format", "PNG8");
            tileInfo.put("compressionQuality", "0");
            Map<String, Double> origin = new HashMap<String, Double>();
            origin.put("x", gridSubset.getGridSetBounds().getMinX());
            origin.put("y", gridSubset.getGridSetBounds().getMaxY());
            tileInfo.put("origin", origin);
            tileInfo.put("spatialReference", Collections.singletonMap("wkid", wkid));
            List<Map<String, Number>> lods = new ArrayList<Map<String, Number>>();
            for (int i = gridSubset.getZoomStart(), max = gridSubset.getZoomStop(); i <= max; i++) {
                Map<String, Number> l = new HashMap<String, Number>();
                Grid grid = gridSubset.getGridSet().getGrid(i);
                l.put("level", i);
                l.put("resolution", grid.getResolution());
                l.put("scale", grid.getScaleDenominator());
                lods.add(l);
            }
            tileInfo.put("lods", lods);
            map.put("tileInfo", tileInfo);
        }
        return JSON.toJSONString(map, SerializerFeature.DisableCircularReferenceDetect);
    }

    public String generateJsapi() {
        StringWriter result = new StringWriter();
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        model.put("serviceName", layer.getName());
        model.put("ctx", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
        model.put("isTiled", true);
        try {
            jsapiTpl.process(model, result);
        } catch (Exception e) {
            LOG.error(e);
        }
        return result.toString();
    }
}
