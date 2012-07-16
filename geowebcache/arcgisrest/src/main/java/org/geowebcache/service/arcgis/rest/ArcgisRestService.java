package org.geowebcache.service.arcgis.rest;

import org.apache.commons.lang.StringUtils;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.Conveyor;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.mime.MimeType;
import org.geowebcache.service.OWSException;
import org.geowebcache.service.Service;
import org.geowebcache.storage.StorageBroker;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:oxsean@gmail.com">sean yang</a>
 * @version V1.0, 12-6-19
 */
public class ArcgisRestService extends Service {

    public static final String SERVICE_ARCGIS_REST = "arcgisrest";

    private StorageBroker sb;

    private TileLayerDispatcher tld;

    private String jsapiUrl;

    public ArcgisRestService() {
        super(SERVICE_ARCGIS_REST);
    }

    public ArcgisRestService(StorageBroker sb, TileLayerDispatcher tld) {
        this();
        this.sb = sb;
        this.tld = tld;
    }

    public void setJsapiUrl(String jsapiUrl) {
        this.jsapiUrl = jsapiUrl;
    }

    @Override
    public Conveyor getConveyor(HttpServletRequest request, HttpServletResponse response) throws GeoWebCacheException, OWSException {
        String[] parts = StringUtils.split(request.getRequestURI().substring(request.getContextPath().length()), '/');
        int len = parts.length;
        switch (len) {
            case 4:
                handleInfo(parts[2], getParam(request, "f", "html"), request.getParameter("callback"), request, response);
                break;
            case 8:
                String col = parts[7];
                int i = col.indexOf('.');
                long colNum = Long.parseLong(i > -1 ? col.substring(0, i) : col);
                return handleTile(
                        parts[2],
                        Long.parseLong(parts[5]),
                        Long.parseLong(parts[6]),
                        colNum, i > -1 ? col.substring(i + 1) : "png",
                        request, response);
            case 5:
                if ("export".equals(parts[4])) {
                    return handleExport(
                            parts[2],
                            getParam(request, "f", "jsapi"),
                            request.getParameter("bbox"),
                            getParam(request, "size", "400,400"),
                            getParam(request, "dpi", "96"),
                            request.getParameter("imageSR"),
                            request.getParameter("bboxSR"),
                            getParam(request, "format", "png"),
                            request.getParameter("layerDefs"),
                            request.getParameter("layers"),
                            ServletRequestUtils.getBooleanParameter(request, "transparent", true),
                            request, response);
                } else {
                    handleLayer(parts[2], parts[4], request, response);
                }
                break;
            default:
                throw new GeoWebCacheException("Bad request [{" + request.getRequestURI() + "}]");
        }
        ConveyorTile tile = new ConveyorTile(sb, parts[2], request, response);
        tile.setRequestHandler(ConveyorTile.RequestHandler.SERVICE);
        return tile;
    }

    @Override
    public void handleRequest(Conveyor conv) throws GeoWebCacheException, OWSException {
        //do nothing
    }

    //contextPath/service/arcgisrest/{serviceName}/MapServer
    private void handleInfo(String serviceName, String f, String callback, HttpServletRequest request, HttpServletResponse response) throws GeoWebCacheException {
        TileLayer tileLayer = tld.getTileLayer(serviceName);
        InfoGenerator gen = new InfoGenerator(request, tileLayer);
        String out;
        if ("json".equals(f)) {
            out = gen.generateJson();
            if (StringUtils.isNotEmpty(callback)) {
                response.setContentType("text/javascript;charset=utf-8");
                out = callback + "(" + out + ");";
            } else {
                response.setContentType("text/json;charset=utf-8");
            }
        } else {
            if (jsapiUrl != null)
                request.setAttribute("_jsapiUrl", jsapiUrl);
            response.setContentType("text/html;charset=utf-8");
            out = gen.generateJsapi();
        }
        try {
            response.getWriter().write(out);
        } catch (IOException ignored) {
        }
    }

    //contextPath/service/arcgisrest/{serviceName}/MapServer/tile/{level}/{row}/{col}
    @SuppressWarnings("unchecked")
    private ConveyorTile handleTile(String serviceName, long level, long row, long col, String format, HttpServletRequest request, HttpServletResponse response) throws GeoWebCacheException, OWSException {
        TileLayer tileLayer = tld.getTileLayer(serviceName);
        String subSetId = tileLayer.getGridSubsets().iterator().next();
        row = tileLayer.getGridSubset(tileLayer.getGridSubsets().iterator().next()).getGridSet().getGrid((int) level).getNumTilesHigh() - 1 - row;
        long[] tileIndex = {col, row, level};
        ConveyorTile tile = new ConveyorTile(sb, serviceName, subSetId, tileIndex, MimeType.createFromFormat("image/" + format), tileLayer.getModifiableParameters(request.getParameterMap(), request.getCharacterEncoding()), request, response);
        tile.setTileLayer(tileLayer);
        return tile;
    }

    //contextPath/service/arcgisrest/{serviceName}/MapServer/{layerId}
    private void handleLayer(String serviceName, String layerId, HttpServletRequest request, HttpServletResponse response) throws GeoWebCacheException {
        throw new GeoWebCacheException("Not support now");
    }

    //contextPath/service/arcgisrest/{serviceName}/MapServer/export
    private ConveyorTile handleExport(String serviceName, String f, String bbox, String size, String dpi, String imageSR, String bboxSR, String format, String layerDefs, String layers, boolean transparent, HttpServletRequest request, HttpServletResponse response) throws GeoWebCacheException {
        throw new GeoWebCacheException("Not support now");
    }

    private String getParam(HttpServletRequest request, String name, String def) {
        String value = request.getParameter(name);
        if (StringUtils.isBlank(value)) {
            return def;
        }
        return value;
    }
}
