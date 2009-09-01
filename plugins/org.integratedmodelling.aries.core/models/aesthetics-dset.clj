(ns aries.models
	(:refer-clojure)
  (:refer modelling :only (defmodel measurement classification ranking identification bayesian)))

(defmodel valuable-waterbodies 'aestheticService:WaterBody

   "Classifies a land use model into lakes, oceans and land"
	(classification (ranking 'nlcd:NLCDNumeric)
	 		23   'aestheticService:Lake
			32   'aestheticService:Ocean
			:otherwise 'aestheticService:NoWater))

;; obvious, for testing
(defmodel altitude-in-meters 'geophysics:Altitude
	(measurement 'geophysics:Altitude "mm"))

(defmodel altitude-transformed 'geophysics:Altitude
	(measurement 'geophysics:Altitude "mm")
		:state (+ self 5)
	)

(defmodel valuable-mountain 'aestheticService:Mountain

   "Classifies an elevation model into three levels of provision of beautiful mountains"
   (classification  (measurement 'geophysics:Altitude "m")
   		[2000 2750]  'aestheticService:SmallMountain  ; 
   		[2750 8850]  'aestheticService:LargeMountain  ; no higher than Mount everest!
   		:otherwise   'aestheticService:NoMountain     ; will catch artifacts too
   		))
   		    		 
(defmodel view-source 'aestheticService:SensoryEnjoyment
	
		"Testing BN integration"
		(bayesian 'aestheticService:SensoryEnjoyment)
			:import  "../aries/plugins/org.integratedmodelling.aries.core/demo/bn/aestheticService_SensoryEnjoyment.xdsl"
		  :keep    ('aestheticService:SensoryEnjoyment)
			:context (valuable-mountain))
   		    		 
(defmodel view-data 'aestheticService:SensoryEnjoyment

 	 "Just the dataset for further processing. This is a test model that merely collects, transforms and
 	  harmonizes data."
 	 (identification 'aestheticService:SensoryEnjoyment)
 	 	 :context
  	 	 (valuable-mountain    :as mountain 
 ; 	 	  valuable-waterbodies :as water))
))