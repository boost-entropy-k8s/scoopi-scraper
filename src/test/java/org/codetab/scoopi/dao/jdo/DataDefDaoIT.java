package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

/**
 * <p>
 * DataDefDao tests.
 * @author Maithilish
 *
 */
public class DataDefDaoIT extends ITBase {

    private DataDefDao dao;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private ObjectFactory objectFactory;

    @Before
    public void setUp() throws Exception {
        dao = new DataDefDao(daoUtil.getPersistenceManagerFactory());

        objectFactory = new ObjectFactory();

        schemaClasses.add("org.codetab.scoopi.model.DataDef");
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testStoreDataDef() {
        Date runDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        Date fromDate = DateUtils.addDays(runDate, -2);
        Date highDate = DateUtils.addYears(runDate, 10);
        // old 1
        DataDef dataDef1 = objectFactory.createDataDef("acme", fromDate,
                highDate, "test json 2");
        // old 2
        fromDate = DateUtils.addDays(runDate, -1);
        DataDef dataDef2 = objectFactory.createDataDef("acme", fromDate,
                highDate, "test json 1");
        // some other
        DataDef dataDef3 = objectFactory.createDataDef("goog", fromDate,
                highDate, "test json 3");
        // new one json changed
        DataDef dataDef4 = objectFactory.createDataDef("acme", runDate,
                highDate, "test json 2");

        dao.storeDataDef(dataDef1);
        dao.storeDataDef(dataDef2);
        dao.storeDataDef(dataDef3);
        dao.storeDataDef(dataDef4);

        List<DataDef> actual = daoUtil.getObjects(DataDef.class,
                Lists.newArrayList("detachDefJson"));

        assertThat(actual.size()).isEqualTo(4);
        assertThat(actual).containsExactly(dataDef1, dataDef2, dataDef3,
                dataDef4);
    }

    @Test
    public void testGetDataDefByName() {
        Date runDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 2);
        DataDef dataDef1 = objectFactory.createDataDef("acme", fromDate, toDate,
                "test json 1");
        DataDef dataDef2 = objectFactory.createDataDef("acme", runDate, toDate,
                "test json 2");
        DataDef dataDef3 = objectFactory.createDataDef("goog", fromDate, toDate,
                "test json 3");

        dao.storeDataDef(dataDef1);
        dao.storeDataDef(dataDef2);
        dao.storeDataDef(dataDef3);

        List<DataDef> actual = dao.getDataDefs("acme");

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactly(dataDef1, dataDef2);

        actual = dao.getDataDefs("goog");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).containsExactly(dataDef3);
    }

    @Test
    public void testGetDataDefsByDate() {
        Date runDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        Date fromDate = DateUtils.addDays(runDate, -2);
        Date toDate = DateUtils.addDays(runDate, -1);
        // old 1
        DataDef dataDef1 = objectFactory.createDataDef("acme", fromDate, toDate,
                "test json 1");
        // old 2
        fromDate = DateUtils.addDays(runDate, -1);
        toDate = DateUtils.addSeconds(runDate, -1);
        DataDef dataDef2 = objectFactory.createDataDef("acme", fromDate, toDate,
                "test json 1");
        // some other
        fromDate = runDate;
        toDate = DateUtils.addDays(runDate, 1);
        DataDef dataDef3 = objectFactory.createDataDef("goog", fromDate, toDate,
                "test json 3");
        // new one json changed
        DataDef dataDef4 = objectFactory.createDataDef("acme", runDate, toDate,
                "test json 2");

        dao.storeDataDef(dataDef1);
        dao.storeDataDef(dataDef2);
        dao.storeDataDef(dataDef3);
        dao.storeDataDef(dataDef4);

        List<DataDef> actual = dao.getDataDefs(runDate);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactly(dataDef3, dataDef4);

        actual = dao.getDataDefs(DateUtils.addHours(runDate, -1));

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).containsExactly(dataDef2);
    }
}
