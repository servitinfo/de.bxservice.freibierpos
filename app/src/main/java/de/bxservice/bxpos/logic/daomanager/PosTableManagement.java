/**********************************************************************
 * This file is part of FreiBier POS                                   *
 *                                                                     *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - Bx Service GmbH                                      *
 **********************************************************************/
package de.bxservice.bxpos.logic.daomanager;

import android.content.Context;

import java.util.List;

import de.bxservice.bxpos.logic.model.idempiere.Table;

/**
 * Created by Diego Ruiz on 28/12/15.
 */
public class PosTableManagement extends AbstractObjectManagement {

    public PosTableManagement(Context ctx) {
        super(ctx);
    }

    @Override
    public Table get(long id){
        return dataMapper.getTable(id);
    }

    @Override
    public boolean remove(Object object) {
        return true;
    }

    public boolean isTableFree(Table table) {
        return dataMapper.isTableFree(table);
    }

    public List<Table> getAllTables() {
        return dataMapper.getAllTables();
    }

}
