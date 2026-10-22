
import { api } from "./api";

export const userService = {
  // Retrieve all users
  getAllUsers() {
    return api.get("/users");
  },

  // Retrieve a single user
  getUserById(id) {
    return api.get(`/users/${id}`);
  },

  // Create a user
  createUser(userData) {
    return api.post("/users", userData);
  },
};
