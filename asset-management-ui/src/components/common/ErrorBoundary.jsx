import React from "react";
import ErrorState from "./ErrorState";
import Button from "./Button";

/**
 * Global ErrorBoundary to catch unexpected runtime errors and prevent blank white screens.
 */
export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("Uncaught runtime error captured by ErrorBoundary:", error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  handleReset = () => {
    this.setState({ hasError: false, error: null });
    window.location.href = "/dashboard";
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-[#F5F8F7] p-6">
          <div className="w-full max-w-lg">
            <ErrorState
              variant="card"
              title="Something went wrong"
              message="The application ran into an unexpected display issue. Please reload the page or return to the dashboard."
              action={
                <div className="flex items-center gap-3">
                  <Button variant="outline" size="sm" onClick={this.handleReload}>
                    Reload Page
                  </Button>
                  <Button variant="primary" size="sm" onClick={this.handleReset}>
                    Go to Dashboard
                  </Button>
                </div>
              }
            />
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

