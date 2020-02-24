package net.ofnir.v14gradle

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.function.ValueProvider
import com.vaadin.flow.router.RouterLayout
import groovy.transform.Immutable

final class Tracer {

    static final List<Tracer.TraceEvent> events = []

    static void trace(final Class source, final String message) {
        def traceEvent = new TraceEvent(source, message)
        events.add(traceEvent)
        fireOnTraceEvent(traceEvent)
    }

    static void clear() {
        events.clear()
    }

    static final List<TraceEventListener> traceEventListeners = []

    static void addTraceEventListener(TraceEventListener listener) {
        traceEventListeners.add(listener)
    }

    static void removeTraceEventListener(TraceEventListener listener) {
        traceEventListeners.remove(listener)
    }

    static void fireOnTraceEvent(TraceEvent traceEvent) {
        traceEventListeners.each {
            it.onTraceEvent(traceEvent)
        }
    }

    static class TraceEvent {
        final String source
        final String message
        TraceEvent(Class source, String message) {
            this.source = source.simpleName
            this.message = message
        }
    }

    static interface TraceEventListener {
        void onTraceEvent(TraceEvent traceEvent)
    }
}

class TracerComponent extends FlexLayout implements Tracer.TraceEventListener {

    final TextField filterField
    final Grid<Tracer.TraceEvent> eventsGrid

    TracerComponent() {
        add(
                filterField = new TextField().tap{
                    setPlaceholder("Filter events")
                    setClearButtonVisible(true)
                    setValueChangeMode(ValueChangeMode.EAGER)
                    addValueChangeListener{
                        if (it.fromClient) {
                            refresh()
                        }
                    }
                },
                eventsGrid = new Grid(Tracer.TraceEvent, false).tap {
                    addColumn(Tracer.TraceEvent::getSource).setHeader("Source")
                    addColumn(Tracer.TraceEvent::getMessage).setHeader("Message")
                    setWidth(30, Unit.EM)
                    addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES)
                },
                new Button("Clear").tap {
                    addClickListener {
                        clear()
                    }
                }
        )
        setHeightFull()
        setFlexDirection(FlexDirection.COLUMN)
        setFlexGrow(1, eventsGrid)
    }

    void clear() {
        Tracer.clear()
        filterField.clear()
        refresh()
    }

    void refresh() {
        eventsGrid.tap{
            setItems(Tracer.events.findAll{
                filter(it.source) || filter(it.message)
            })
            scrollToEnd()
        }
    }

    boolean filter(String v) {
        filterField.value ? v.containsIgnoreCase(filterField.value) : true
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent)
        Tracer.addTraceEventListener(this)
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent)
        Tracer.removeTraceEventListener(this)
    }

    @Override
    void onTraceEvent(Tracer.TraceEvent traceEvent) {
        refresh()
    }
}

class TracerLayout extends FlexLayout implements RouterLayout {

    final TracerComponent tracerComponent

    TracerLayout() {
        setSizeFull()
        tracerComponent = new TracerComponent()
    }

    @Override
    void showRouterLayoutContent(HasElement content) {
        removeAll()
        add(tracerComponent)
        super.showRouterLayoutContent(content)
    }
}
