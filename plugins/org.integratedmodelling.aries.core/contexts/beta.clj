;;Proximity model for San Pedro
(ns core.contexts.beta
  (:refer-clojure :rename {count length})
  (:refer modelling :only [defcontext model])
  (:refer geospace :only [grid]))

(defcontext chehalis
  "Chehalis watershed @ 256 linear. Just the square bounding box."
  (grid 
    256 
    "EPSG:4326 POLYGON ((-124.27 46.763, -124.27 47.55, -122.42 47.55, -122.42 46.763, -124.27 46.763))"))

(defcontext chehalis_med_res
  "Chehalis watershed @ 256 linear. Just the square bounding box."
  (grid 
    512 
    "EPSG:4326 POLYGON ((-124.27 46.763, -124.27 47.55, -122.42 47.55, -122.42 46.763, -124.27 46.763))"))

(defcontext chehalis_hi_res
  "Chehalis watershed @ 256 linear. Just the square bounding box."
  (grid 
    1024 
    "EPSG:4326 POLYGON ((-124.27 46.763, -124.27 47.55, -122.42 47.55, -122.42 46.763, -124.27 46.763))"))

(defcontext raven_ridge 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-73.437902 45.016731, -71.465281 45.016731, -71.465281 42.727110, -73.437902 42.727110, -73.437902 45.016731))"))

(defcontext raven_ridge_large 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-74.439038 45.188716, -71.877970 45.188716, -71.877970 43.346402, -74.439038 43.346402,-74.439038 45.188716))"))    

(defcontext san_pedro 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 30.869, -111.012 30.869, -111.012 33.281))"))

(defcontext san_pedro_med_res 
  "" 
  (grid  
    512 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 30.869, -111.012 30.869, -111.012 33.281))"))

(defcontext san_pedro_bsr 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-110.401 32.037, -110.193 32.037, -110.193 31.855, -110.401 31.855, -110.401 32.037))"))

(defcontext san_pedro_us
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 31.338, -111.012 31.338, -111.012 33.281))"))

(defcontext san_pedro_us_low_res
  "" 
  (grid  
    128
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 31.338, -111.012 31.338, -111.012 33.281))"))

(defcontext san_pedro_us_med_res
  "" 
  (grid  
    512 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 31.338, -111.012 31.338, -111.012 33.281))"))

(defcontext san_pedro_us_hi_res
  "" 
  (grid  
    1024 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 31.338, -111.012 31.338, -111.012 33.281))"))

(defcontext san_pedro_us_super_hi_res
  "" 
  (grid  
    2048 
    "EPSG:4326 POLYGON((-111.012 33.281, -109.872 33.281, -109.872 31.338, -111.012 31.338, -111.012 33.281))"))

(defcontext san_pedro_sprnca 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-110.377 31.881, -109.987 31.881, -109.987 31.336, -110.377 31.336, -110.377 31.881))"))

(defcontext la_antigua 
  "" 
  (grid  
    1024 
    "EPSG:4326 POLYGON ((-97.2746372601786 19.142240226562222, -96.27930746109703 19.142240226562222, -96.27930746109703 19.576122241899572, -97.2746372601786 19.576122241899572,-97.2746372601786 19.142240226562222))"))

(defcontext vt 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-73.437902 45.016731, -71.465281 45.016731, -71.465281 42.727110, -73.437902 42.727110, -73.437902 45.016731))"))

(defcontext vtcoverage 
  "" 
  (grid 
    256 
    "EPSG:4326 POLYGON ((-73.151 44.27 ,-73.144  44.27 ,-73.144  44.286 , -73.151 44.286 ,-73.151  44.27 ))"))

(defcontext DR 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-71.7 19.9, -70.5 19.9, -70.5 18.85, -71.7 18.85, -71.7 19.9))"))

(defcontext DRsm 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-71.7 19.9, -70.5 19.9, -70.5 18.85, -71.7 18.85, -71.7 19.9))"))

(defcontext DR_hi_res 
  "" 
  (grid  
    512 
    "EPSG:4326 POLYGON((-71.7 19.9, -70.5 19.9, -70.5 18.85, -71.7 18.85, -71.7 19.9))"))

;(defcontext chehalis 
;  "" 
;  (grid  
;    256 
;    "EPSG:4326 POLYGON((-124.27 47.55, -124.27 46.33, -122.42 46.33, -122.42 47.55, -124.27 47.55))"))

(defcontext viewshed 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-122.420446 47.464349, -121.759593 47.464349, -121.759593 46.85382, -122.420446 46.85382, -122.420446 47.464349))"))

(defcontext viewshed_med_res
  "" 
  (grid  
    512 
    "EPSG:4326 POLYGON((-122.420446 47.464349, -121.759593 47.464349, -121.759593 46.85382, -122.420446 46.85382, -122.420446 47.464349))"))

(defcontext viewshed_hi_res
  "" 
  (grid  
    1024 
    "EPSG:4326 POLYGON((-122.420446 47.464349, -121.759593 47.464349, -121.759593 46.85382, -122.420446 46.85382, -122.420446 47.464349))"))

(defcontext western_wa
  ""
  (grid
   256
    "EPSG:4326 POLYGON ((-124.88 46.3, -124.88 49.11, -120.6 49.11, -120.6 46.3, -124.88 46.3))")) 

(defcontext western_wa_hi_res
  ""
  (grid
   2048
    "EPSG:4326 POLYGON ((-124.88 46.3, -124.88 49.11, -120.6 49.11, -120.6 46.3, -124.88 46.3))")) 

(defcontext mg 
  "" 
  (grid 
    256 
    "EPSG:4326 POLYGON((41.352539056744014 -27.644606378394307, 52.778320305152796 -27.644606378394307, 52.778320305152796 -10.488, 41.352539056744014 -10.488, 41.352539056744014 -27.644606378394307))"))

(defcontext mg_coastal
  ""
   (grid
    256
    "EPSG:4326 POLYGON ((47.0 -16.4, 50.9 -16.4, 50.9 -20.2, 47.0 -20.2, 47.0 -16.4))"))  

(defcontext mg_coastal_lowres
  ""
   (grid
    128
    "EPSG:4326 POLYGON ((47.0 -16.4, 50.9 -16.4, 50.9 -20.2, 47.0 -20.2, 47.0 -16.4))"))

(defcontext mg_coastal_hires
  ""
   (grid
    512
    "EPSG:4326 POLYGON ((47.0 -16.4, 50.9 -16.4, 50.9 -20.2, 47.0 -20.2, 47.0 -16.4))"))

(defcontext mg_sed
  ""
   (grid
    1024
    "EPSG:4326 POLYGON ((52.778320305152796 -10, 52.778320305152796 -27.644606378394307, 41.352539056744014 -27.644606378394307, 41.352539056744014 -15.934637411351126, 52.778320305152796 -10))"))  

(defcontext lye_brook 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-73.097615 43.167086, -72.938627 43.167086, -72.938627 43.051168, -73.097615 43.051168, -73.097615 43.167086))")) 

(defcontext usa_bbox 
  "" 
  (grid 256 
    "EPSG:4326 POLYGON((-124.7625 24.5210, -66.9326  24.5210, -66.9326 49.3845, -124.7625 49.3845, -124.7625 24.5210))"))

(defcontext europe_bbox 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-21.2660 27.6363, -21.2660 61.0088, 39.8693 61.0088, 39.8693 27.6363, -21.2660 27.6363))"))

(defcontext agri 
  "" 
  (grid 
    256 
    "EPSG:4326 POLYGON((15.544968 40.455699, 15.919189 40.190414, 16.11557 40.336077, 15.770874 40.605612, 15.544968 40.455699))"))

(defcontext ca_mark 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-117.976828 33.907017, -117.976828  33.89560825, -117.940320  33.89560825, -117.940320 33.907017, -117.976828 33.907017))"))

(defcontext ca_mark_watershed 
  "" 
  (grid  
    256 
    "EPSG:4326 POLYGON((-118.2 34.03787, -117.724 34.03787, -117.724 33.739, -118.2 33.739, -118.2 34.03787))"))

(defcontext ca_mark_watershed_hi_res 
  "" 
  (grid  
    512 
    "EPSG:4326 POLYGON((-118.2 34.03787, -117.724 34.03787, -117.724 33.739, -118.2 33.739, -118.2 34.03787))"))
