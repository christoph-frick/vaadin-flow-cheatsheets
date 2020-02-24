package net.ofnir.v14gradle

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

    AttachedContent(Class rootClass) {
        this.rootClass = rootClass
        Tracer.trace(rootClass, "Content c'tor")
        add(new Span("Content for ${rootClass.simpleName}"))
    }

    @Override
    void beforeLeave(BeforeLeaveEvent event) {
        Tracer.trace(rootClass, "Content BeforeLeaveObserver")
    }

    @Override
    void beforeEnter(BeforeEnterEvent event) {
        Tracer.trace(rootClass, "Content BeforeEnterObserver")
    }

    @Override
    void afterNavigation(AfterNavigationEvent _) {
        Tracer.trace(rootClass, "Content AfterNavigationObserver")
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
        add(new AttachedContent(OuterLayout))
        add(new Div(new RouterLink("Home", HomeView)))
        add(new Div(new RouterLink("Another View", AnotherView)))
        add(new Div(new RouterLink("Parameter View (no param)", ParameterView)))
        add(new Div(new RouterLink("Parameter View (with param)", ParameterView, "Param")))
        add(new Div(new RouterLink("Wildcard Parameter View", WildcardParameterView)))
    }

}

@ParentLayout(OuterLayout)
class InnerLayout extends AbstractLayout implements TracingObservers {

    InnerLayout() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(InnerLayout))
    }

}

@Route(value = "", layout = InnerLayout)
class HomeView extends Div implements TracingObservers {

    HomeView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(HomeView))
    }

}

@Route(value = "another", layout = InnerLayout)
class AnotherView extends Div implements TracingObservers {

    AnotherView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(AnotherView))
    }

}

@Route(value = "param", layout = InnerLayout)
class ParameterView extends Div implements TracingObservers, HasUrlParameter<String> {

    ParameterView() {
        Tracer.trace(getClass(), "c'tor")
        add(new AttachedContent(ParameterView))
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
        add(new AttachedContent(WildcardParameter))
    }

    @Override
    void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        Tracer.trace(getClass(), "setParameter(${parameter})")
    }
}
