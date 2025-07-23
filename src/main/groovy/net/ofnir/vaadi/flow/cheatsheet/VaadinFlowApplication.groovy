package net.ofnir.vaadi.flow.cheatsheet

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationListener
import com.vaadin.flow.router.AfterNavigationObserver
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterListener
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.BeforeLeaveEvent
import com.vaadin.flow.router.BeforeLeaveListener
import com.vaadin.flow.router.BeforeLeaveObserver
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.ListenerPriority
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.router.WildcardParameter
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.spring.annotation.EnableVaadin
import groovy.transform.InheritConstructors
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableVaadin
@Push
class VaadinFlowApplication implements AppShellConfigurator {

    static void main(String[] args) {
        SpringApplication.run(VaadinFlowApplication, args)
    }

    @Bean
    VaadinServiceInitListener vaadinServiceInitListener() {
        new VaadinServiceInitListener() {
            @Override
            void serviceInit(ServiceInitEvent serviceInitEvent) {
                serviceInitEvent.source.addUIInitListener(new UIInitListener() {
                    @Override
                    void uiInit(UIInitEvent uIInitEvent) {
                        [
                                new LowPriorityListener(),
                                new AveragePriorityListener(),
                                new HighPriorityListener(),
                        ].each { listener ->
                            uIInitEvent.UI.tap {
                                addBeforeEnterListener(listener)
                                addBeforeLeaveListener(listener)
                                addAfterNavigationListener(listener)
                            }

                        }
                    }
                })
            }
        }
    }

}

class PriorityListener implements BeforeLeaveListener, BeforeEnterListener, AfterNavigationListener {

    PriorityListener() {
        Tracer.trace(getClass(), "c'tor")
    }

    @Override
    void beforeLeave(BeforeLeaveEvent event) {
        Tracer.trace(getClass(), "BeforeLeaveListener")
    }

    @Override
    void beforeEnter(BeforeEnterEvent event) {
        Tracer.trace(getClass(), "BeforeEnterListener")
    }

    @Override
    void afterNavigation(AfterNavigationEvent _) {
        Tracer.trace(getClass(), "AfterNavigationListener")
    }

}

@ListenerPriority(5)
class HighPriorityListener extends PriorityListener {
}

@ListenerPriority(0)
class AveragePriorityListener extends PriorityListener {
}

@ListenerPriority(-5)
class LowPriorityListener extends PriorityListener {
}

class AttachedContent extends Div implements BeforeLeaveObserver, BeforeEnterObserver, AfterNavigationObserver {

    final Class rootClass
    final String marker

    AttachedContent(Class rootClass, int nr) {
        this.rootClass = rootClass
        this.marker = "Content #$nr"
        Tracer.trace(rootClass, mark("c'tor"))
        add(new Span(mark("for ${rootClass.simpleName}")))
    }

    String mark(String text) {
        "$marker: $text"
    }

    @Override
    void beforeLeave(BeforeLeaveEvent event) {
        Tracer.trace(rootClass, mark("BeforeLeaveObserver"))
    }

    @Override
    void beforeEnter(BeforeEnterEvent event) {
        Tracer.trace(rootClass, mark("BeforeEnterObserver"))
    }

    @Override
    void afterNavigation(AfterNavigationEvent _) {
        Tracer.trace(rootClass, mark("AfterNavigationObserver"))
    }

}

class AttachedContentLayout extends Div {

    AttachedContentLayout(Class rootClass, int startNr) {
        def parts = (1..3).collect { new AttachedContent(rootClass, startNr + it) }
        // intentionally break the order to find out, if insertion order or placement inside the DOM wins
        add(parts[0])
        addComponentAtIndex(0, parts[1])
        add(parts[2])
    }
}

abstract class AbstractLayout extends Composite<Div> implements RouterLayout {

    final Div header = new Div()

    final Div layoutContent = new Div()

    AbstractLayout() {
        header.element.style.tap {
            set('border', '1px solid grey')
        }
        [header, layoutContent].each {
            it.element.style.tap {
                set('padding', 'var(--lumo-space-m)')
            }
            content.add it
        }
    }

    @Override
    void showRouterLayoutContent(HasElement content) {
        layoutContent.tap {
            removeAll()
            element.appendChild(content.element)
        }
    }

    void add(Component c) {
        header.add(c)
    }
}

trait TracingObservers implements BeforeLeaveObserver, BeforeEnterObserver, AfterNavigationObserver {
    @Override
    void beforeLeave(BeforeLeaveEvent event) {
        Tracer.trace(getClass(), "BeforeLeaveEvent")
    }

    @Override
    void beforeEnter(BeforeEnterEvent event) {
        Tracer.trace(getClass(), "BeforeEnterObserver")
    }

    @Override
    void afterNavigation(AfterNavigationEvent _) {
        Tracer.trace(getClass(), "AfterNavigationObserver")
    }
}

@ParentLayout(TracerLayout)
class OuterLayout extends AbstractLayout implements TracingObservers {

    OuterLayout() {
        Tracer.trace(getClass(), "c'tor")
        add(new Div(new RouterLink("Home", HomeView)))
        add(new Div(new RouterLink("Another View", AnotherView)))
        add(new Div(new RouterLink("Nested attached listeners", NestedAttachedListenersView)))
        add(new Div(new RouterLink("Parameter View (no param)", ParameterView)))
        add(new Div(new RouterLink("Parameter View (with param)", ParameterView, "Param")))
        add(new Div(new RouterLink("Wildcard Parameter View", WildcardParameterView)))
        add(new AttachedContent(OuterLayout, 1))
    }

}

@ParentLayout(OuterLayout)
class InnerLayout extends AbstractLayout implements TracingObservers {

    InnerLayout() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(InnerLayout, 10))
    }

}

@Route(value = "", layout = InnerLayout)
class HomeView extends Div implements TracingObservers {

    HomeView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(HomeView, 100))
    }

}

@Route(value = "another", layout = InnerLayout)
class AnotherView extends Div implements TracingObservers {

    AnotherView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(AnotherView, 100))
    }

}

@Route(value = "nestedAttachedListeners", layout = InnerLayout)
class NestedAttachedListenersView extends Div implements TracingObservers {

    NestedAttachedListenersView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContentLayout(NestedAttachedListenersView, 100))
        add(
                new Div(
                        new AttachedContent(NestedAttachedListenersView, 1000),
                        new AttachedContentLayout(NestedAttachedListenersView, 2000),
                ),
                new Div(
                        new AttachedContentLayout(NestedAttachedListenersView, 3000),
                        new AttachedContent(NestedAttachedListenersView, 4000),
                ),
        )
    }

}

@Route(value = "param", layout = InnerLayout)
class ParameterView extends Div implements TracingObservers, HasUrlParameter<String> {

    ParameterView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(ParameterView, 100))
    }

    @Override
    void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Tracer.trace(getClass(), "setParameter(${parameter})")
    }
}

@Route(value = "wildcard", layout = InnerLayout)
class WildcardParameterView extends Div implements TracingObservers, HasUrlParameter<String> {

    WildcardParameterView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(WildcardParameter, 100))
    }

    @Override
    void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        Tracer.trace(getClass(), "setParameter(${parameter})")
    }
}
