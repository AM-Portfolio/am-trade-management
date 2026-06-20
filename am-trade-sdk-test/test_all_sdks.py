#!/usr/bin/env python3
"""
Master Test Runner for AM Trade SDKs

This script runs both Java and Python SDK verifications in sequence,
providing a unified testing interface and comprehensive reporting.
"""

import subprocess
import sys
import os

# Fix Unicode encoding on Windows
if sys.platform == 'win32':
    # Set UTF-8 encoding for stdout
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from datetime import datetime
from pathlib import Path


class SDKTestRunner:
    """Runs verification tests for both Java and Python SDKs"""
    
    def __init__(self):
        # Current directory is am-trade-sdk-test
        self.test_dir = Path(__file__).resolve().parent
        self.repo_root = self.test_dir.parent
        self.start_time = datetime.now()
        self.results = {}
    
    def print_header(self):
        """Print test runner header"""
        print("\n" + "=" * 80)
        print("🚀 AM TRADE SDK - UNIFIED TEST RUNNER")
        print("=" * 80)
        print(f"Repository: {self.repo_root}")
        print(f"Start Time: {self.start_time.strftime('%Y-%m-%d %H:%M:%S')}")
        print("=" * 80 + "\n")
    
    def run_java_tests(self):
        """Run Java SDK verifier tests"""
        print("\n" + "─" * 80)
        print("📘 JAVA SDK VERIFICATION")
        print("─" * 80 + "\n")
        
        java_dumper = self.test_dir / "java-dumper"
        
        if not java_dumper.exists():
            print("❌ Java dumper not found at:", java_dumper)
            self.results['java'] = {
                'status': 'SKIPPED',
                'reason': 'Java dumper directory not found',
                'details': f"Expected path: {java_dumper}"
            }
            return False
        
        try:
            print(f"📂 Directory: {java_dumper}")
            print("Running Java SDK verification...\n")
            
            # Run Maven tests for Java SDK from the repo root
            # Use mvnw.cmd on Windows, mvnw on Unix
            mvn_cmd = 'mvnw.cmd' if sys.platform == 'win32' else './mvnw'
            
            result = subprocess.run(
                [mvn_cmd, 'clean', 'test', '-f', 'am-trade-sdk-core/pom.xml', '-q'],
                cwd=str(self.repo_root),
                capture_output=True,
                text=True,
                timeout=300
            )
            
            if result.returncode == 0:
                print("✅ Java SDK tests PASSED")
                self.results['java'] = {
                    'status': 'PASSED',
                    'reason': 'All tests passed',
                    'output': result.stdout[-500:] if result.stdout else ''
                }
                return True
            else:
                print("❌ Java SDK tests FAILED")
                print("\nError output:")
                print(result.stderr[-1000:] if result.stderr else 'No error output')
                self.results['java'] = {
                    'status': 'FAILED',
                    'reason': 'Maven tests failed',
                    'error': result.stderr[-500:] if result.stderr else ''
                }
                return False
                
        except subprocess.TimeoutExpired:
            print("❌ Java SDK tests TIMEOUT (exceeded 5 minutes)")
            self.results['java'] = {
                'status': 'TIMEOUT',
                'reason': 'Test execution exceeded timeout',
                'timeout': '300 seconds'
            }
            return False
        except Exception as e:
            print(f"❌ Error running Java tests: {e}")
            self.results['java'] = {
                'status': 'ERROR',
                'reason': str(e),
                'type': type(e).__name__
            }
            return False
    
    def run_python_tests(self):
        """Run Python SDK verifier tests"""
        print("\n" + "─" * 80)
        print("🐍 PYTHON SDK VERIFICATION")
        print("─" * 80 + "\n")
        
        python_verifier = self.test_dir / "python-verifier" / "verify.py"
        
        if not python_verifier.exists():
            print("❌ Python verifier not found at:", python_verifier)
            self.results['python'] = {
                'status': 'SKIPPED',
                'reason': 'Python verifier script not found',
                'details': f"Expected path: {python_verifier}"
            }
            return False
        
        try:
            print(f"📂 Script: {python_verifier}")
            print("Running Python SDK verification...\n")
            
            # Run Python verifier
            result = subprocess.run(
                [sys.executable, str(python_verifier)],
                cwd=str(python_verifier.parent),
                capture_output=True,
                text=True,
                timeout=120,
                env={**os.environ, 'PYTHONUNBUFFERED': '1'}
            )
            
            # Print output for user visibility
            if result.stdout:
                print(result.stdout)
            
            if result.returncode == 0:
                print("\n✅ Python SDK tests PASSED")
                self.results['python'] = {
                    'status': 'PASSED',
                    'reason': 'All verifications passed',
                    'output': result.stdout[-500:] if result.stdout else ''
                }
                return True
            else:
                print("\n❌ Python SDK tests FAILED")
                if result.stderr:
                    print("\nError output:")
                    print(result.stderr[-1000:])
                self.results['python'] = {
                    'status': 'FAILED',
                    'reason': 'Verification failed',
                    'error': result.stderr[-500:] if result.stderr else ''
                }
                return False
                
        except subprocess.TimeoutExpired:
            print("❌ Python SDK tests TIMEOUT (exceeded 2 minutes)")
            self.results['python'] = {
                'status': 'TIMEOUT',
                'reason': 'Test execution exceeded timeout',
                'timeout': '120 seconds'
            }
            return False
        except Exception as e:
            print(f"❌ Error running Python tests: {e}")
            self.results['python'] = {
                'status': 'ERROR',
                'reason': str(e),
                'type': type(e).__name__
            }
            return False
    
    def print_summary(self):
        """Print test results summary"""
        print("\n" + "=" * 80)
        print("📊 TEST RESULTS SUMMARY")
        print("=" * 80 + "\n")
        
        # Results table
        print(f"{'SDK':<20} {'Status':<15} {'Details':<45}")
        print("─" * 80)
        
        for sdk, result in self.results.items():
            status = result.get('status', 'UNKNOWN')
            reason = result.get('reason', 'N/A')
            status_emoji = {
                'PASSED': '✅',
                'FAILED': '❌',
                'ERROR': '⚠️ ',
                'TIMEOUT': '⏱️ ',
                'SKIPPED': '⊘'
            }.get(status, '?')
            
            print(f"{sdk:<20} {status_emoji} {status:<12} {reason:<45}")
        
        print("\n" + "=" * 80)
        
        # Overall status
        all_passed = all(r.get('status') == 'PASSED' for r in self.results.values())
        
        if all_passed:
            print("✅ ALL TESTS PASSED - SDKs are ready for use!")
        else:
            failed = [k for k, v in self.results.items() if v.get('status') != 'PASSED']
            print(f"❌ SOME TESTS FAILED: {', '.join(failed)}")
        
        # Timing
        elapsed = (datetime.now() - self.start_time).total_seconds()
        print(f"\n⏱️  Total execution time: {elapsed:.2f} seconds")
        
        print("\n" + "=" * 80)
    
    def run_all(self):
        """Run all verifications"""
        self.print_header()
        
        # Run both tests
        java_ok = self.run_java_tests()
        python_ok = self.run_python_tests()
        
        # Print summary
        self.print_summary()
        
        # Exit with appropriate code
        return 0 if (java_ok and python_ok) else 1


def main():
    """Main entry point"""
    try:
        runner = SDKTestRunner()
        exit_code = runner.run_all()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\n⚠️  Test execution interrupted by user")
        sys.exit(130)
    except Exception as e:
        print(f"\n\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
