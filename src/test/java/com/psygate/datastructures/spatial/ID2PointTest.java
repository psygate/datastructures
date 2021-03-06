/*
 * General datastructures.
 * Copyright (C) 2015  psygate (https://github.com/psygate)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
package com.psygate.datastructures.spatial;

import com.psygate.datastructures.spatial.Axis2D;
import com.psygate.datastructures.spatial.ID2Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class ID2PointTest {

    public ID2PointTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getX method, of class IDPoint.
     */
    @Test
    public void testGetX() {
        ID2Point point = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };

        assertEquals(3, point.getX(), 0);
    }

    /**
     * Test of getY method, of class IDPoint.
     */
    @Test
    public void testGetY() {
        ID2Point point = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };

        assertEquals(5, point.getY(), 0);
    }

    /**
     * Test of distSqr method, of class IDPoint.
     */
    @Test
    public void testDistSqr() {
        assertEquals(25, new ID2Point() {
            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }
        }.distSqr(new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 4;
            }
        }), 0);
    }

    /**
     * Test of dist method, of class IDPoint.
     */
    @Test
    public void testDist() {
        assertEquals(5, new ID2Point() {
            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }
        }.dist(new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 4;
            }
        }), 0);
    }

    /**
     * Test of get method, of class IDPoint.
     */
    @Test
    public void testGet() {
        ID2Point point = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };

        assertEquals(3, point.get(Axis2D.X), 0);
        assertEquals(5, point.get(Axis2D.Y), 0);
    }

    /**
     * Test of get method, of class IDPoint.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetThrows() {
        ID2Point point = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };

        assertEquals(3, point.get(null), 0);
    }

    /**
     * Test of same method, of class IDPoint.
     */
    @Test
    public void testSame() {
        ID2Point point = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };
        ID2Point point2 = new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }
        };
        ID2Point point3 = new ID2Point() {
            @Override
            public double getX() {
                return 4;
            }

            @Override
            public double getY() {
                return 5;
            }
        };
        assertTrue(point.same(point));
        assertTrue(point.same(point2));
        assertFalse(point.same(point3));
        assertFalse(point.same(new ID2Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 6;
            }
        }));
        assertFalse(point.same(new ID2Point() {
            @Override
            public double getX() {
                return 2;
            }

            @Override
            public double getY() {
                return 5;
            }
        }));
    }

}
