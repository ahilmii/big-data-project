import {
  BrowserRouter,
  NavLink,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { GraduationCap, Sparkles } from "lucide-react";
import Management from "./pages/Management";
import Chat from "./pages/Chat";
import "./App.css";
export default function App() {
  return (
    <BrowserRouter>
      <header className="topbar">
        <div className="brand">
          <GraduationCap size={30} />
          <span>
            School Management<small>SWE 307 · Campus workspace</small>
          </span>
        </div>
        <nav aria-label="Ana menü">
          <NavLink to="/" end>
            School Management
          </NavLink>
          <NavLink to="/chat">
            <Sparkles size={17} /> AI Chat
          </NavLink>
        </nav>
      </header>
      <Routes>
        <Route path="/" element={<Management />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <footer>SWE 307 · School Management</footer>
    </BrowserRouter>
  );
}
