import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  Card,
  TextField,
  Typography,
} from "eworldes-ui-toolkit-v1.1.0";
import { useAuth } from "../../hooks/useAuth";

export default function Login() {
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const { username, password } = formData;
    const result = login(username, password);
    if (result.success) {
      navigate("/dashboard");
    }
  };

  return (
    <div className="login-container">
        <div className="left-panel">
          <video autoPlay muted loop playsInline className="login-video">
            <source src="/loginPage-gif.mp4" type="video/mp4" />
            Your browser does not support the video tag.
          </video>

        </div>
      
        
        <div className="right-panel">
          <Card className="login-card">
          <div className="login-logo flex gap-0.5 items-center justify-center mb-4 p-2">
            <img src="/title.png" alt="Logo" className="login-logo-img h-24" />
          </div>
          <div className="login-form flex flex-col gap-5  justify-center items-center">
            <Typography
              variant="h4"
              className="login-heading !text-sm !font-bold !font-body"
            >
              Welcome Back! Please Sign In to Your Account
            </Typography>
            <form
              className="login-form flex flex-col gap-5 w-full"
              onSubmit={handleSubmit}
            >
              <TextField
                label="Username"
                name="username"
                type="text"
                value={formData.username}
                onChange={handleChange}
                fullWidth
              />

              <TextField
                label="Password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                fullWidth
              />

              {error && (
                <Typography variant="body2" className="!text-red-600">
                  {error}
                </Typography>
              )}
              
              <Button
                type="submit"
                variant="contained"
                fullWidth
                disabled={loading}
              >
                {loading ? "Signing in..." : "Login"}
              </Button>
            </form>
          </div>
        </Card>
        </div>
      
    </div>
  );
}
