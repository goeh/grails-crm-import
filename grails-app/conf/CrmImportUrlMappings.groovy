class CrmImportUrlMappings {

	static mappings = {
        "/crmImport/create/$importer"{
            controller = "crmImport"
            action = "create"
            constraints {
                importer(validator: {grailsApplication.mainContext.containsBean(it)})
            }
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
