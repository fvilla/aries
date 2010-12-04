(ns core.models.aesthetic-view-san-pedro
  (:refer-clojure :rename {count length})
  (:refer modelling :only [defscenario defmodel measurement classification categorization ranking
                           numeric-coding binary-coding identification bayesian count])
  (:refer aries :only [span]))

;; ----------------------------------------------------------------------------------------------
;; Source model
;; ----------------------------------------------------------------------------------------------

(defmodel mountain 'aestheticService:Mountain
  (classification (measurement 'geophysics:Altitude "m")
                  [1400 1800]  'aestheticService:SmallMountain  
                  [1800 8850]  'aestheticService:LargeMountain ;; no higher than Mt. Everest, catches artifacts
                  :otherwise   'aestheticService:NoMountain))  ;; catches low artifacts

(defmodel scenic-vegetation 'sanPedro:VegetationType
;;  [(categorization 'geofeatures:Country :as country)]
  (classification (numeric-coding 'sanPedro:SouthwestRegionalGapAnalysisLULC) 
;;                  :when #(= (:country %) "United States")
                  #{1 2 3 4 5 6 7 8 9 15 39 69 70 71 86 89}               'sanPedro:AlpineAndCliff
                  #{22 23 33 37 38 91}                                    'sanPedro:Forest
                  #{34 35 36 41 42 44 46 63 64 92 95 100 101 102 103 109} 'sanPedro:Woodland ;; includes pinon & juniper savannas
                  #{77 78 79 80 81 83 84 85 98 109 110 118}               'sanPedro:RiparianAndWater
                  :otherwise                                              'sanPedro:Other))
;;  (classification (categorization 'mexico:CONABIOLULCCategory)
;;                  #{"Bosque de coniferas distintas a Pinus" "Bosque de encino" "Bosque de pino"} 'sanPedro:Forest
;;                  #{"Vegetacion de galeria"}                                                     'sanPedro:Woodland
;;                  #{"Cuerpos de agua"}                                                           'sanPedro:RiparianAndWater
;;                  :otherwise                                                                     'sanPedro:Other))

(defmodel theoretical-beauty 'aestheticService:TheoreticalNaturalBeauty
  (classification 'aestheticService:TheoreticalNaturalBeauty
                  [0    5] 'aestheticService:NoNaturalBeauty 
                  [5   25] 'aestheticService:LowNaturalBeauty 
                  [25  50] 'aestheticService:ModerateNaturalBeauty 
                  [50 100] 'aestheticService:HighNaturalBeauty))

;; source bayesian model	    		 
(defmodel source 'aestheticService:AestheticViewProvision
  "This one will harmonize the context, then retrieve and run the BN with the given
   evidence, and produce a new observation with distributions for the requested nodes."
  (bayesian 'aestheticService:AestheticViewProvision 
            :import   "aries.core::ViewSourceSanPedro.xdsl"
            :context  (mountain scenic-vegetation)
            :observed (theoretical-beauty)
            :keep     ('aestheticService:TheoreticalNaturalBeauty)))

;; ----------------------------------------------------------------------------------------------
;; Sink model
;; ----------------------------------------------------------------------------------------------

(defmodel mine 'aestheticService:Mines                         
  (classification (numeric-coding 'sanPedro:SouthwestRegionalGapAnalysisLULC)         
                  #{19 117}       'aestheticService:MinesPresent
                  :otherwise      'aestheticService:MinesAbsent))

(defmodel transmission-line 'aestheticService:TransmissionLines 
  (classification (binary-coding 'infrastructure:TransmissionLine)
                  1          'aestheticService:TransmissionLinesPresent
                  :otherwise 'aestheticService:TransmissionLinesAbsent))

(defmodel highway 'aestheticService:Highways 
  (classification (binary-coding 'infrastructure:Highway)
                  0          'aestheticService:HighwaysAbsent
                  :otherwise 'aestheticService:HighwaysPresent))

;; Insert correct concepts for Mexico
(defmodel developed-land 'aestheticService:DevelopedLand
  (classification (numeric-coding 'sanPedro:SouthwestRegionalGapAnalysisLULC)           
                  111        'aestheticService:LowDensityDevelopment
                  112        'aestheticService:HighDensityDevelopment
                  :otherwise 'aestheticService:NoDevelopment))

(defmodel view-sink-undiscretizer 'aestheticService:VisualBlight
  (classification 'aestheticService:VisualBlight
                  [0    5]  'aestheticService:NoBlight 
                  [5   25]  'aestheticService:LowBlight 
                  [25  50]  'aestheticService:ModerateBlight 
                  [50 100]  'aestheticService:HighBlight))

(defmodel sink 'aestheticService:ViewSink
  "Landscape features that reduce the quality and enjoyment of scenic views"
  (bayesian 'aestheticService:ViewSink 
            :import  "aries.core::ViewSinkSanPedro.xdsl"
            :context  (mine highway transmission-line developed-land)
            :observed (view-sink-undiscretizer) 
            :keep     ('aestheticService:VisualBlight)))

;; ----------------------------------------------------------------------------------------------
;; Use model
;; ----------------------------------------------------------------------------------------------

(defmodel housing 'aestheticService:PresenceOfHousing
  (classification (ranking 'economics:AppraisedPropertyValue)
                  0               'aestheticService:HousingAbsent
                  :otherwise      'aestheticService:HousingPresent))

(defmodel property-value 'aestheticService:HousingValue  ;; value is in $/ac, which is not a legitimate unit in thinklab, so kept as a ranking for now.
  (classification (ranking 'economics:AppraisedPropertyValue)
                  [:<        50000] 'aestheticService:VeryLowHousingValue
                  [50000    100000] 'aestheticService:LowHousingValue
                  [100000   350000] 'aestheticService:ModerateHousingValue
                  [350000  1000000] 'aestheticService:HighHousingValue
                  [1000000 :>]      'aestheticService:VeryHighHousingValue))

;;Scenic highways as another beneficiary class - i.e., their drivers benefit from views along highways.
(defmodel scenic-highways 'aestheticService:ScenicDrivePresence
  (classification (binary-coding 'aestheticService:ScenicDrives)
                1             'aestheticService:ScenicDrivesPresent
                :otherwise    'aestheticService:ScenicDrivesAbsent))

;;undiscretizer for view use
(defmodel view-use-undiscretizer 'aestheticService:HomeownerViewUse
  (classification 'aestheticService:HomeownerViewUse
                  [0 5]   'aestheticService:HomeownerViewUseAbsent 
                  [5 100] 'aestheticService:HomeownerViewUsePresent))

;; bayesian model
(defmodel homeowners 'aestheticService:ViewUse
  "Property owners who can afford to pay for the view"
  (bayesian 'aestheticService:ViewUse 
            :import  "aries.core::ViewUseSanPedro.xdsl"
            :context (property-value housing)
            :observed (view-use-undiscretizer) 
            :keep    ('aestheticService:HomeownerViewUse)))

;; ----------------------------------------------------------------------------------------------
;; Dependencies for the flow model
;; ----------------------------------------------------------------------------------------------

(defmodel altitude 'geophysics:Altitude
  (measurement 'geophysics:Altitude "m"))	 								

;; ---------------------------------------------------------------------------------------------------	 	 	
;; Top-level service models
;; ---------------------------------------------------------------------------------------------------	 	 	

;; all data, for testing and storage
(defmodel data-homeowners 'aestheticService:LineOfSight
  (identification 'aestheticService:LineOfSight
                  :context (source          :as source
                            homeowners      :as use 
                            sink            :as sink
                            altitude        :as altitude)))

(defmodel data-scenic-highways 'aestheticService:LineOfSight
  (identification 'aestheticService:LineOfSight
                  :context (source          :as source
                            scenic-highways :as use  
                            sink            :as sink
                            altitude        :as altitude)))

;; the real enchilada - need to be updated to the latest SPAN language
(defmodel view 'aestheticService:AestheticView
  (span 'aestheticService:LineOfSight 
        'aestheticService:TheoreticalNaturalBeauty
        'aestheticService:HomeownerViewUse
      	'aestheticService:VisualBlight
      	nil
        'geophysics:Altitude
        ;;:source-threshold   100.0  ;;Initially set as the midpoint of the lowest bin
        ;;:sink-threshold     450.0  ;;Initially set as the midpoint of the lowest bin
        ;;:use-threshold      0.0    ;;Set at zero since output values for this are a 0/1
        ;;:trans-threshold    10.0   ;;Set at an initially arbitrary but low weight; eventually run sensitivity analysis on this
        :source-type      :infinite
        :sink-type        :infinite
        :use-type         :infinite
        :benefit-type     :non-rival
        :downscaling-factor 3
        :rv-max-states      10
        :keep ('aestheticService:PotentialViews 'aestheticService:PotentialVisualBlight 'aestheticService:HomeownersWithViewDemand
               'aestheticService:PossibleViews 'aestheticService:VisibleNaturalBeauty 'aestheticService:HomeownersWithPossibleViews
               'aestheticService:ActualViews 'aestheticService:EnjoyedViews 'aestheticService:RelevantVisualBlight
               'aestheticService:HomeownersWithViews 'aestheticService:UnseenViews 'aestheticService:InaccessibleVisualBlight
               'aestheticService:HomeownersWithoutViews 'aestheticService:BlockedViews 'aestheticService:DegradedNaturalBeauty
               'aestheticService:HomeownersWithDegradedViews)
        :context (source homeowners sink altitude)))

;;Develop another one of these to account for scenic drives.

;; ----------------------------------------------------------------------------------------------
;; Scenarios

;; Observations that are specifically tagged for a scenario will be picked up automatically
;; instead of the baseline ones.
;; ----------------------------------------------------------------------------------------------

;;(defscenario mesquite-management 'sanPedro:MesquiteManagement): change scenic-vegetation to sanPedro:Other within polygons in the source model.
      
;;(defscenario urban-growth 'sanPedro:UrbanGrowth: change scenic-vegetation to sanPedro:Other within polygons in the source model.  
;;  Add new developed-land as aestheticService:LowDensityDevelopment in the sink model.  Add more users in the developed areas in the use model.
      ;;sanPedro:UrbanGrowth2020Open
      ;;sanPedro:UrbanGrowth2020Constrained
      
;;(defscenario bsr-development 'sanPedro:BSRDevelopment: change scenic-vegetation to sanPedro:Other within polygons in the source model.
;;  Add new developed-land as aestheticService:LowDensityDevelopment in the sink model.  Add more users in the developed areas in the use model.
      ;;sanPedro:BSRDevelopmentSite1
      ;;sanPedro:BSRDevelopmentSite2
      ;;sanPedro:BSRDevelopmentSite3
      ;;sanPedro:BSRDevelopmentSite4
      ;;sanPedro:BSRDevelopmentSite5
