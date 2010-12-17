;; --------------------------------------------------------------------------------------------------
;; UNEP marine project
;; models for coastal protection
;; --------------------------------------------------------------------------------------------------

(ns marine.models.coastal
  (:refer-clojure :rename {count length})
  (:refer modelling :only (defscenario defmodel measurement classification categorization
                           ranking numeric-coding binary-coding identification bayesian count))
  (:refer aries :only (span)))

;; --------------------------------------------------------------------------------------
;; Source models
;; --------------------------------------------------------------------------------------

;;(defmodel storm-probability 'coastalProtection:TropicalStormProbability
;;  (ranking 'habitat:TropicalStormProbability))

(defmodel storm-tracks 'coastalProtection:StormTracks
  (categorization 'coastalProtection:StormTracks)) 

(defmodel buffer 'coastalProtection:BufferMg100km
  (binary-coding 'coastalProtection:BufferMg100km)) 

(defmodel bathymetry-class 'coastalProtection:BathymetryClass
  (classification (measurement 'geophysics:Bathymetry "m")
    [0 :>]              'coastalProtection:Overland
    [0 -20]             'coastalProtection:VeryShallow
    [-20 -50]           'coastalProtection:Shallow
    [-50 -200]          'coastalProtection:Deep
    [:< -200]           'coastalProtection:VeryDeep))

(defmodel atmospheric-pressure 'coastalProtection:AtmosphericPressureClass
  (classification (ranking 'geophysics:AtmosphericPressure) ;;This should be a measurement, in mb, but this is not yet a valid unit in Thinklab.
    [990 :>]  'coastalProtection:ModeratelyLowAtmosphericPressure
    [970 990] 'coastalProtection:LowAtmosphericPressure
    [:< 970]  'coastalProtection:VeryLowAtmosphericPressure))

;;Discretization based on the Southwest Indian Ocean Tropical Cyclone Scale. May need a different
;; scale for other parts of the world.
(defmodel wind-speed 'coastalProtection:WindSpeedClass
  (classification (measurement 'geophysics:WindSpeed "km/h") 
     [165 :>]    'coastalProtection:VeryHighWindSpeed
     [117 165]   'coastalProtection:HighWindSpeed
     [88 117]    'coastalProtection:ModeratelyHighWindSpeed
     [62 88]     'coastalProtection:ModeratelyLowWindSpeed
     [50 62]     'coastalProtection:LowWindSpeed
     [:< 50]     'coastalProtection:VeryLowWindSpeed))

;;Undiscretization & BN statements
(defmodel storm-surge 'coastalProtection:StormSurgeClass
  (classification 'coastalProtection:StormSurgeClass
                  :units      "m"
;;                  [5 :>]      'coastalProtection:VeryHighStormSurge
                  [5 6]      'coastalProtection:VeryHighStormSurge
                  [4 5]       'coastalProtection:HighStormSurge
                  [3 4]       'coastalProtection:ModerateStormSurge
                  [2 3]       'coastalProtection:LowStormSurge
                  [1 2]       'coastalProtection:VeryLowStormSurge
;;                  [:< 1]      'coastalProtection:NoStormSurge))
                  [0 1]      'coastalProtection:NoStormSurge))

(defmodel coastal-wave-source 'coastalProtection:CoastalWaveSource
    "Interface to Flood public asset use bayesian network"
    (bayesian 'coastalProtection:CoastalWaveSource
      :import   "aries.marine::CoastalFloodSource.xdsl"
      :keep     ('coastalProtection:StormSurgeClass)
      :observed (storm-surge)
      :context  (wind-speed atmospheric-pressure bathymetry-class)))

;;This takes the BN values ONLY where they intersect the 100 km buffer along the storm track for a given storm (Daisy in the first case)
(defmodel source-100km-daisy 'coastalProtection:CoastalWaveSourceDaisy
  (measurement 'coastalProtection:CoastalWaveSourceDaisy
    :context (storm-tracks        :as storm-tracks
              buffer              :as buffer
              coastal-wave-source :as coastal-wave-source)
    :state  #(if (and (= (:storm-tracks %) "daisy")
                      (= (:buffer %) 1))
                 (:coastal-wave-source %)
                 0.0)))

(defmodel source-100km-geralda 'coastalProtection:CoastalWaveSourceGeralda
  (measurement 'coastalProtection:CoastalWaveSourceGeralda
    :context (storm-tracks        :as storm-tracks
              buffer              :as buffer
              coastal-wave-source :as coastal-wave-source)
    :state  #(if (and (= (:storm-tracks %) "geralda")
                      (= (:buffer %) 1))
                 (:coastal-wave-source %)
                 0.0)))

(defmodel source-100km-litanne 'coastalProtection:CoastalWaveSourceLitanne
  (measurement 'coastalProtection:CoastalWaveSourceLitanne
    :context (storm-tracks        :as storm-tracks
              buffer              :as buffer
              coastal-wave-source :as coastal-wave-source)
    :state  #(if (and (= (:storm-tracks %) "litanne")
                      (= (:buffer %) 1))
                 (:coastal-wave-source %)
                 0.0)))

;; --------------------------------------------------------------------------------------
;; Sink (coastal protection) model
;; --------------------------------------------------------------------------------------

(defmodel mangrove 'coastalProtection:MangrovePresenceClass
  (classification (numeric-coding 'mglulc:MGLULCNumeric)
    5          'coastalProtection:MangrovePresent
    :otherwise 'coastalProtection:MangroveAbsent))

;; Most polygons do not report any data on bleaching, these are placed under "moderate bleaching".  There's a misspelling for "HIgh" in the data that's accounted for in the discretization below.
(defmodel coral-quality 'coastalProtection:CoralQuality
  (classification (categorization 'coastalProtection:CoralBleaching)
         #{"None" "Low"}      'coastalProtection:MinimallyBleachedCoralPresent
         #{"HIgh" "High"}     'coastalProtection:HighlyBleachedCoralPresent
         #{"Moderate" ""}     'coastalProtection:ModeratelyBleachedCoralPresent
         :otherwise           'coastalProtection:NoCoralPresent))
 
;; TODO only two classes represented from presence/absence; no idea how to 
;; model density based on existing data.
(defmodel seagrass 'coastalProtection:SeagrassPresenceClass
	(classification (binary-coding 'coastalProtection:SeagrassPresence)
		0 'coastalProtection:SeagrassAbsent
		1 'coastalProtection:SeagrassPresent))

;;Terrestrial vegetation types from Mg LULC layer
(defmodel terrestrial-vegetation 'coastalProtection:TerrestrialVegetationType
  (classification (numeric-coding 'mglulc:MGLULCNumeric)
         #{1 3 4 8 10 20 21 23 30 31}             'coastalProtection:Forested ;;Includes tree-dominated savannas
         #{6 7}                                   'coastalProtection:Shrubland
         #{14}                                    'coastalProtection:Wetland
         #{9 11 12 13 18 22 24 25 26 28 29 32 33} 'coastalProtection:Herbaceous ;;Includes agriculture, grass-dominated savannas
         #{16 17 19 27}                           'coastalProtection:Other)) 

;;Assumes some artificial flood protection near Toamasina, the main port city in Madagascar.  Development around the small ports is minimal.
(defmodel artificial-coastal-protection 'coastalProtection:ArtificialCoastalProtection
  (classification (binary-coding 'infrastructure:Port)
    3          'coastalProtection:ArtificialCoastalProtectionPresent
    :otherwise 'coastalProtection:ArtificialCoastalProtectionAbsent))

;;The discretization below is a first cut, may need to be changed based on results of the flow model.
(defmodel coastal-flood-protection 'coastalProtection:TotalCoastalFloodProtection
  (classification 'coastalProtection:TotalCoastalFloodProtection
                  :units      "m"
                  [1 :>]        'coastalProtection:HighCoastalFloodProtection
                  [0.5 1]       'coastalProtection:ModerateCoastalFloodProtection
                  [0.1 0.5]     'coastalProtection:LowCoastalFloodProtection
                  [0 0.1]       'coastalProtection:NoCoastalFloodProtection))

;; Wave mitigation by ecosystems, i.e., the ecosystem service.
(defmodel coastal-flood-sink 'coastalProtection:CoastalFloodSink
  	"Interface to Flood public asset use bayesian network"
	  (bayesian 'coastalProtection:CoastalFloodSink 
	  	:import   "aries.marine::CoastalFloodSink.xdsl"
	  	:keep     ('coastalProtection:TotalCoastalFloodProtection)
        :observed (coastal-flood-protection)
        :context  (mangrove coral-quality seagrass terrestrial-vegetation artificial-coastal-protection)))

;; --------------------------------------------------------------------------------------
;; Use models
;; --------------------------------------------------------------------------------------

;;Returns the deciles of risk to life and property
(defmodel risk-to-life 'coastalProtection:CycloneDependentLivesAtRisk
	(ranking 'policytarget:LivesAtRiskStorm))

(defmodel risk-to-assets 'coastalProtection:CycloneSensitiveEconomicValue
	(ranking 'policytarget:AssetsAtRiskStorm))

;; --------------------------------------------------------------------------------------
;; Flow models
;; --------------------------------------------------------------------------------------

(defmodel dune 'coastalProtection:DunePresenceClass
  (classification (binary-coding 'geofeatures:Dune)
    #{"dune"}   'coastalProtection:DunePresent    
    :otherwise  'coastalProtection:DuneAbsent))

(defmodel slope 'coastalProtection:BathymetricSlope
  (classification (measurement 'geophysics:BathymetricSlope "\u00b0")
    [16.70 :>]      'coastalProtection:HighSlope
    [4.57 16.70]    'coastalProtection:ModerateSlope
    [1.15 4.57]     'coastalProtection:LowSlope
    [:< 1.15]       'coastalProtection:VeryLowSlope))

(defmodel depth-elevation 'coastalProtection:OceanDepthAndLandElevation
  (classification (measurement 'geophysics:Bathymetry "m")
    [20 :>]             'coastalProtection:HighLandElevation
    [5 20]              'coastalProtection:ModerateLandElevation
    [0 5]               'coastalProtection:LowLandElevation
    [0 -60]             'coastalProtection:Pelagic
    [-60 -200]          'coastalProtection:Shelf
    [-200 -2000]        'coastalProtection:Slope
    [:< -2000]          'coastalProtection:DeepWater))

;;The discretization below is a first cut, may need to be changed based on results of the flow model.
(defmodel geomorphic-flood-protection 'coastalProtection:GeomorphicFloodProtection
  (classification 'coastalProtection:GeomorphicFloodProtection
                  :units      "m"
;;                  [1 :>]        'coastalProtection:HighGeomorphicProtection
                  [1   2]       'coastalProtection:HighGeomorphicProtection
                  [0.5 1]       'coastalProtection:ModerateGeomorphicProtection
                  [0 0.5]       'coastalProtection:LowGeomorphicProtection))

;; Wave mitigation by geomorphic features (i.e., baseline wave mitigation in the absence of ecosystems)
(defmodel geomorphic-flood-sink 'coastalProtection:GeomorphicWaveReduction
    "Interface to Flood public asset use bayesian network"
    (bayesian 'coastalProtection:GeomorphicWaveReduction 
      :import   "aries.marine::CoastalFloodSink.xdsl"
      :keep     ('coastalProtection:GeomorphicFloodProtection)
      :observed (geomorphic-flood-protection)
      :context  (dune depth-elevation slope)))

(defmodel coastal-flow-data 'coastalProtection:CoastalFlowData$
  (identification 'coastalProtection:CoastalFlowData
      :context (storm-tracks geomorphic-flood-protection)))

(defmodel coastal-protection-data 'coastalProtection:CoastalStormProtection
	(identification 'coastalProtection:CoastalStormProtection 
		:context (coastal-flood-sink risk-to-life risk-to-assets coastal-wave-source geomorphic-flood-sink)))

;;This SPAN statement has just been copied from flood_mark, but the "keep" 
;; list has been updated to correctly reflect the coastal flood flow concepts.
;;Could have as many as 6 SPAN statements: one each for risk-to-life & risk-to-assets, 1 each for 3 storm events.
(defmodel coastal-protection-flow 'coastalProtection:CoastalStormProtection
  (span 'coastalProtection:CoastalStormMovement
        'coastalProtection:CoastalWaveSourceDaisy
        'coastalProtection:CycloneDependentLivesAtRisk
        'coastalProtection:CoastalFloodSink
        nil 
        'coastalProtection:CoastalFlowData
        :source-threshold   0.0
        :sink-threshold     0.0
        :use-threshold      0.0
        :trans-threshold    10.0   ;;Set at an initially arbitrary but low weight; eventually run sensitivity analysis on this
        :source-type        :finite
        :sink-type          :infinite
        :use-type           :infinite
        :benefit-type       :non-rival
        :downscaling-factor 1
        :rv-max-states      10
        :keep ('coastal:CoastalWaveSource 'coastal:PotentialWaveMitigation 'coastal:PotentiallyWaveVulnerablePopulations
              'coastal:PotentiallyDamagingWaveFlow 'coastal:PotentiallyDamagingWaveSource 'coastal:PotentialFloodDamageReceived
              'coastal:ActualWaveFlow 'coastal:FloodDamagingWaveSource 'coastal:UtilizedWaveMitigation
              'coastal:FloodDamageReceived 'coastal:BenignWaveSource 'coastal:UnutilizedWaveMitigation
              'coastal:AbsorbedWaveFlow 'coastal:MitigatedWaveSource 'coastal:FloodMitigationBenefitsAccrued) 
        ;;:save-file          (str (System/getProperty "user.home") "/flood_data_farmers100.clj")
        :context (source-100km-daisy risk-to-life coastal-flood-sink)))
