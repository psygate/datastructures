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

import com.psygate.datastructures.spatial.ID3Point;
import com.psygate.datastructures.spatial.Axis3D;
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
public class ID3PointTest {

    public ID3PointTest() {
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
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };

        assertEquals(3, point.getX(), 0);
    }

    /**
     * Test of getY method, of class IDPoint.
     */
    @Test
    public void testGetY() {
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };

        assertEquals(5, point.getY(), 0);
    }

    /**
     * Test of getZ method, of class IDPoint.
     */
    @Test
    public void testGetZ() {
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };

        assertEquals(12, point.getZ(), 0);
    }

    /**
     * Test of distSqr method, of class IDPoint.
     */
    @Test
    public void testDistSqr() {
        assertEquals(3 * 3 + 4 * 4 + 5 * 5, new ID3Point() {
            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }

            @Override
            public double getZ() {
                return 0;
            }
        }.distSqr(new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 4;
            }

            @Override
            public double getZ() {
                return 5;
            }
        }), 0);
    }

    /**
     * Test of dist method, of class IDPoint.
     */
    @Test
    public void testDist() {
        assertEquals(Math.sqrt(3 * 3 + 4 * 4 + 5 * 5), new ID3Point() {
            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }

            @Override
            public double getZ() {
                return 0;
            }
        }.dist(new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 4;
            }

            @Override
            public double getZ() {
                return 5;
            }
        }), 0);
    }

    /**
     * Test of get method, of class IDPoint.
     */
    @Test
    public void testGet() {
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };

        assertEquals(3, point.get(Axis3D.X), 0);
        assertEquals(5, point.get(Axis3D.Y), 0);
        assertEquals(12, point.get(Axis3D.Z), 0);
    }

    /**
     * Test of get method, of class IDPoint.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetThrows() {
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };

        assertEquals(3, point.get(null), 0);
    }

    /**
     * Test of same method, of class IDPoint.
     */
    @Test
    public void testSame() {
        ID3Point point = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };
        ID3Point point2 = new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };
        ID3Point point3 = new ID3Point() {
            @Override
            public double getX() {
                return 4;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        };
        assertTrue(point.same(point));
        assertTrue(point.same(point2));
        assertFalse(point.same(point3));
        assertFalse(point.same(new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 6;
            }

            @Override
            public double getZ() {
                return 12;
            }
        }));
        assertFalse(point.same(new ID3Point() {
            @Override
            public double getX() {
                return 2;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 12;
            }
        }));

        assertFalse(point.same(new ID3Point() {
            @Override
            public double getX() {
                return 3;
            }

            @Override
            public double getY() {
                return 5;
            }

            @Override
            public double getZ() {
                return 13;
            }
        }));
    }

}
