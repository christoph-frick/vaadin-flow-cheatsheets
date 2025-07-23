package net.ofnir.vaadi.flow.cheatsheet

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.SpringVaadinSession
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.VaadinSessionScope

@SpringComponent
@VaadinSessionScope
@Route('session-inspector')
class SessionInspector extends Div implements AfterNavigationObserver {

    private final SpringVaadinSession session

    SessionInspector(SpringVaadinSession session) {
        this.session = session
    }

    @Override
    void afterNavigation(AfterNavigationEvent event) {
        removeAll()
        session.UIs.first().internals.stateTree
    }
}
