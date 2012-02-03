import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

class NavigationServiceTests extends GroovyTestCase {
	static transactional = false

    /*
     * Test the config by convention case, where only "navigation = true" is set
     */
    void testConventionNavigation() {
        NavigationService navigationService = new NavigationService()
        assertEquals 0, navigationService.byGroup['*'].size

        def controller = new DefaultGrailsControllerClass(EmptyController.class)
        navigationService.registerItem(controller)

        assertEquals 1, navigationService.byGroup['*'].size
        def item = navigationService.byGroup['*'][0]
        assertEquals "Empty", item.title
        assertEquals "index", item.action
    }

    /*
     * Test to make sure that when a specific list is used to cofigure the navigation
     * only that list actually makes it into the group, without any spares.
     */
    void testOnlyTheListNavigation() {
        NavigationService navigationService = new NavigationService()
        assertEquals 0, navigationService.byGroup['*'].size

        EmptyController.navigation = [
            [ action: 'index', order: 10 ]
        ]

        def controller = new DefaultGrailsControllerClass(EmptyController.class)
        navigationService.registerItem(controller)

        assertEquals 1, navigationService.byGroup['*'].size
    }

    /*
     * Test that the navigation title defaults to the natural version of the specified action
     */
    void testListNavigation() {
        NavigationService navigationService = new NavigationService()
        assertEquals 0, navigationService.byGroup['*'].size

        EmptyController.navigation = [
            [ action: 'index', order: 10 ]
        ]

        def controller = new DefaultGrailsControllerClass(EmptyController.class)
        navigationService.registerItem(controller)

        def item = navigationService.byGroup['*'][0]
        assertEquals "Index", item.title
        assertEquals "index", item.action
    }

    /*
     * Test that the navigation title can be overridden
     */
    void testListTitleNavigation() {
        NavigationService navigationService = new NavigationService()
        assertEquals 0, navigationService.byGroup['*'].size

        EmptyController.navigation = [
            [ action: 'index', title: 'Home', order: 10 ]
        ]

        def controller = new DefaultGrailsControllerClass(EmptyController.class)
        navigationService.registerItem(controller)

        def item = navigationService.byGroup['*'][0]
        assertEquals "Home", item.title
        assertEquals "index", item.action
    }
}

class EmptyController {
    static navigation = true
}

