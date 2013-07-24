/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.web.toolkit.ui;

import com.haulmont.cuba.web.gui.data.PropertyValueStringify;
import com.haulmont.cuba.web.toolkit.data.TableContainer;
import com.haulmont.cuba.web.toolkit.ui.client.table.CubaTableState;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ActionManager;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author artamonov
 * @version $Id$
 */
public class CubaTable extends com.vaadin.ui.Table implements TableContainer {

    protected LinkedList<Object> editableColumns = null;

    protected ActionManager shortcutsManager = new ActionManager();

    @Override
    protected CubaTableState getState() {
        return (CubaTableState) super.getState();
    }

    @Override
    protected CubaTableState getState(boolean markAsDirty) {
        return (CubaTableState) super.getState(markAsDirty);
    }

    public boolean isTextSelectionEnabled() {
        return getState(false).textSelectionEnabled;
    }

    public void setTextSelectionEnabled(boolean textSelectionEnabled) {
        if (isTextSelectionEnabled() != textSelectionEnabled) {
            getState(true).textSelectionEnabled = textSelectionEnabled;
        }
    }

    public boolean isAllowPopupMenu() {
        return getState(false).allowPopupMenu;
    }

    public void setAllowPopupMenu(boolean allowPopupMenu) {
        if (isAllowPopupMenu() != allowPopupMenu) {
            getState(true).allowPopupMenu = allowPopupMenu;
        }
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
        if (property instanceof PropertyValueStringify) {
            return ((PropertyValueStringify) property).getFormattedValue();
        }

        return super.formatPropertyValue(rowId, colId, property);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        // Actions
        if (shortcutsManager != null) {
            shortcutsManager.handleActions(variables, this);
        }
    }

    public Object[] getEditableColumns() {
        if (editableColumns == null) {
            return null;
        }
        return editableColumns.toArray();
    }

    public void setEditableColumns(Object[] editableColumns) {
        checkNotNull(editableColumns, "You cannot set null as editable columns");

        if (this.editableColumns == null) {
            this.editableColumns = new LinkedList<>();
        } else {
            this.editableColumns.clear();
        }

        final Collection properties = getContainerPropertyIds();
        for (final Object editableColumn : editableColumns) {
            if (editableColumn == null) {
                throw new NullPointerException("Ids must be non-nulls");
            } else if (!properties.contains(editableColumn)
                    || columnGenerators.containsKey(editableColumn)) {
                throw new IllegalArgumentException(
                        "Ids must exist in the Container and it must be not a generated column, incorrect id: "
                                + editableColumn);
            }
            this.editableColumns.add(editableColumn);
        }

        refreshRowCache();
    }

    @Override
    protected boolean isColumnEditable(Object columnId, boolean editable) {
        return editable &&
                editableColumns != null && editableColumns.contains(columnId);
    }

    @Override
    public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
        if (generatedColumn == null) {
            throw new IllegalArgumentException(
                    "Can not add null as a GeneratedColumn");
        }
        if (columnGenerators.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Can not add the same GeneratedColumn twice, id:" + id);
        } else {
            columnGenerators.put(id, generatedColumn);
            /*
             * add to visible column list unless already there (overriding
             * column from DS)
             */
            if (!visibleColumns.contains(id)) {
                visibleColumns.add(id);
            }

            if (editableColumns != null) {
                editableColumns.remove(id);
            }

            refreshRowCache();
        }
    }

    @Override
    protected void paintActions(PaintTarget target, Set<Action> actionSet) throws PaintException {
        super.paintActions(target, actionSet);
        shortcutsManager.paintActions(null, target);
    }

    @Override
    protected boolean changeVariables(Map<String, Object> variables) {
        boolean clientNeedsContentRefresh = super.changeVariables(variables);

        if (variables.containsKey("resetsortorder")) {
            resetSortOrder();

            markAsDirty();
        }

        return clientNeedsContentRefresh;
    }

    @Override
    public void addShortcutListener(ShortcutListener listener) {
        /*if (listener.getKeyCode() != 13 || !(listener.getModifiers() == null || listener.getModifiers().length > 0)) {*/
            shortcutsManager.addAction(listener);
        /*} else
            shortcutListeners.add(listener);*/
    }

    @Override
    public void removeShortcutListener(ShortcutListener listener) {
        /*shortcutListeners.remove(listener);*/
        shortcutsManager.removeAction(listener);
    }

    @Override
    public void resetSortOrder() {
        sortContainerPropertyId = null;
        sortAscending = true;

        if (items instanceof TableContainer) {
            ((TableContainer) items).resetSortOrder();
        }
    }
}