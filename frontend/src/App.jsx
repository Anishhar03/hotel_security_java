import React, { useEffect, useState } from "react";

const defaultAuth = {
  username: "staff",
  password: "staff123"
};

export function App() {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [auth, setAuth] = useState(defaultAuth);
  const [newRoom, setNewRoom] = useState({
    number: "",
    type: "",
    status: "AVAILABLE"
  });

  const authHeader = () => {
    const token = btoa(`${auth.username}:${auth.password}`);
    return { Authorization: `Basic ${token}` };
  };

  const fetchRooms = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await fetch("/api/rooms", {
        headers: authHeader()
      });
      if (!res.ok) {
        throw new Error(`Failed to fetch rooms (${res.status})`);
      }
      const data = await res.json();
      setRooms(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await fetch("/api/admin/rooms", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeader()
        },
        body: JSON.stringify(newRoom)
      });
      if (!res.ok) {
        throw new Error(`Failed to create room (${res.status})`);
      }
      setNewRoom({ number: "", type: "", status: "AVAILABLE" });
      await fetchRooms();
    } catch (e) {
      setError(e.message);
    }
  };

  const handleDelete = async (number) => {
    setError("");
    try {
      const res = await fetch(`/api/admin/rooms/${encodeURIComponent(number)}`, {
        method: "DELETE",
        headers: authHeader()
      });
      if (!res.ok && res.status !== 404) {
        throw new Error(`Failed to delete room (${res.status})`);
      }
      await fetchRooms();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div className="page">
      <header className="header">
        <h1>Hotel Rooms</h1>
        <p className="subtitle">Java backend + file DB + React frontend</p>
      </header>

      <section className="card">
        <h2>Authentication</h2>
        <div className="auth-grid">
          <label>
            Username
            <input
              value={auth.username}
              onChange={(e) => setAuth({ ...auth, username: e.target.value })}
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={auth.password}
              onChange={(e) => setAuth({ ...auth, password: e.target.value })}
            />
          </label>
          <button onClick={fetchRooms}>Sign in & Refresh</button>
        </div>
        <p className="hint">
          Default users: <code>staff/staff123</code> (view) and{" "}
          <code>admin/admin123</code> (view + manage).
        </p>
      </section>

      <section className="layout">
        <div className="card">
          <div className="card-header">
            <h2>Rooms</h2>
            <button onClick={fetchRooms} disabled={loading}>
              Refresh
            </button>
          </div>
          {loading && <p>Loading…</p>}
          {error && <p className="error">{error}</p>}
          {!loading && rooms.length === 0 && <p>No rooms yet.</p>}
          <ul className="room-list">
            {rooms.map((room) => (
              <li key={room.number} className="room-item">
                <div>
                  <div className="room-number">Room {room.number}</div>
                  <div className="room-meta">
                    <span>{room.type}</span>
                    <span className={`status status-${room.status?.toLowerCase()}`}>
                      {room.status}
                    </span>
                  </div>
                </div>
                <button
                  className="danger"
                  onClick={() => handleDelete(room.number)}
                >
                  Delete
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div className="card">
          <h2>Add / Update Room (admin)</h2>
          <form className="form" onSubmit={handleCreate}>
            <label>
              Room number
              <input
                required
                value={newRoom.number}
                onChange={(e) =>
                  setNewRoom({ ...newRoom, number: e.target.value })
                }
              />
            </label>
            <label>
              Type
              <input
                required
                placeholder="Deluxe, Suite…"
                value={newRoom.type}
                onChange={(e) =>
                  setNewRoom({ ...newRoom, type: e.target.value })
                }
              />
            </label>
            <label>
              Status
              <select
                value={newRoom.status}
                onChange={(e) =>
                  setNewRoom({ ...newRoom, status: e.target.value })
                }
              >
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="OCCUPIED">OCCUPIED</option>
                <option value="MAINTENANCE">MAINTENANCE</option>
              </select>
            </label>
            <button type="submit">Save room</button>
          </form>
        </div>
      </section>
    </div>
  );
}

